package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.GROUP_UUID;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.LOGICAL_PRODUCT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.STUDY_UNIT_UUID;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedItem;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaResponse;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnitResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listings et recherches du catalogue Colectica : tout ce qui s'obtient par un {@code _query} global
 * ou par une marche de relations, sans passer par la conversion DDI3 → DDI4.
 */
class ColecticaCatalogRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaCatalogRepository.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;
    private final ColecticaLabels labels;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;

    ColecticaCatalogRepository(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaClient colecticaClient,
        ColecticaLabels labels,
        DDI3toDDI4ConverterService ddi3ToDdi4Converter
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
        this.labels = labels;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
    }

    /** Une StudyUnit et les PhysicalInstances qu'elle référence, telles que Colectica les renvoie. */
    private record StudyUnitFragments(
        ColecticaItemResponse studyUnit,
        List<ColecticaItemResponse> physicalInstances
    ) {
        List<String> fragmentXmls() {
            List<String> xmls = new ArrayList<>();
            xmls.add(studyUnit.item());
            xmls.addAll(ColecticaItems.fragmentXmls(physicalInstances));
            return xmls;
        }
    }

    /** Fabrique d'un « partial » à partir des colonnes communes à tous les listings. */
    @FunctionalInterface
    private interface PartialFactory<T> {
        T create(String id, String label, Date versionDate, String agency);
    }

    List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Getting physical instances from Colectica API via HTTP (primary instance)");
        return queryPartials(itemType(PHYSICAL_INSTANCE), PartialPhysicalInstance::new);
    }

    List<PartialLogicalProduct> getLogicalProducts() {
        logger.info("Getting logical products from Colectica API via HTTP (primary instance)");
        return queryPartials(itemType(LOGICAL_PRODUCT), PartialLogicalProduct::new);
    }

    List<PartialStudyUnit> getStudyUnits() {
        logger.info("Getting study units from Colectica API via HTTP");
        return queryPartials(STUDY_UNIT_UUID, PartialStudyUnit::new);
    }

    /**
     * {@code _query} global de tous les CodeListSchemes, qui porte les libellés (contrairement aux
     * descriptions de relation). Sert aussi à résoudre les libellés des schemes d'un logical product.
     */
    List<PartialCodeListScheme> getCodeListSchemes() {
        logger.info("Getting code list schemes from Colectica API via HTTP (primary instance)");
        return queryPartials(itemType(CODE_LIST_SCHEME), PartialCodeListScheme::new);
    }

    /**
     * Les groupes, avec leurs {@code seriesIris} : les libellés viennent du {@code _query}, les IRI
     * de série d'un unique {@code item/_getList} sur l'ensemble des groupes.
     */
    List<PartialGroup> getGroups() {
        logger.info("Getting groups from Colectica API via HTTP");

        ColecticaResponse response = colecticaClient.query(List.of(GROUP_UUID));
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return List.of();
        }

        Map<String, List<String>> seriesIrisByGroupId = seriesIrisByGroupId(response.results());

        return response.results().stream()
            .map(item -> new PartialGroup(
                item.identifier(),
                labels.of(item),
                ColecticaDates.parse(item.versionDate()),
                item.agencyId(),
                seriesIrisByGroupId.getOrDefault(item.identifier(), List.of())))
            .toList();
    }

    private Map<String, List<String>> seriesIrisByGroupId(List<ColecticaItem> groups) {
        ColecticaItemResponse[] itemResponses =
            colecticaClient.getDescriptions(ColecticaItems.identifiersOf(groups));
        Map<String, List<String>> seriesIrisByGroupId = new HashMap<>();
        if (itemResponses != null) {
            for (ColecticaItemResponse itemResponse : itemResponses) {
                seriesIrisByGroupId.put(
                    itemResponse.identifier(),
                    ColecticaXml.userIds(itemResponse.item()).stream().filter(iri -> !iri.isBlank()).toList());
            }
        }
        return seriesIrisByGroupId;
    }

    /**
     * Même listing que {@link #getPhysicalInstances()} mais issu de l'endpoint {@code _query/advanced}
     * de Colectica, qui renvoie les sacs de propriétés par item (en particulier
     * {@code DateProperties.versionDate}).
     */
    List<PartialPhysicalInstance> getPhysicalInstancesViaAdvancedQuery() {
        logger.info("Getting physical instances from Colectica API via HTTP (_query/advanced)");

        ColecticaAdvancedResponse response = colecticaClient.queryAdvanced(
            List.of(itemType(PHYSICAL_INSTANCE)));

        if (response == null || response.results() == null) {
            return List.of();
        }

        return response.results().stream()
            .map(item -> new PartialPhysicalInstance(
                item.identifier(),
                labels.ofAdvanced(item),
                advancedVersionDate(item),
                item.agencyId()))
            .toList();
    }

    /** Première valeur de {@code DateProperties.versionDate}, en UTC tronquée à la seconde. */
    private Date advancedVersionDate(ColecticaAdvancedItem item) {
        if (item.dateProperties() == null) {
            return null;
        }
        List<String> versionDates = item.dateProperties().get("versionDate");
        if (versionDates == null || versionDates.isEmpty()) {
            return null;
        }
        return ColecticaDates.parse(versionDates.getFirst());
    }

    /**
     * Descente {@code bysubject} Group → StudyUnit → PhysicalInstance. Bien moins d'appels que la
     * remontée par PI (qui coûtait 2 requêtes relationnelles par instance) : un appel par groupe
     * ramène toutes ses StudyUnits (avec leurs libellés via {@code findRelatedItems}), puis un appel
     * par StudyUnit ramène les références de ses PhysicalInstances. Les libellés et la
     * {@code versionDate} des PI proviennent de la requête avancée globale (un seul appel), qui sert
     * aussi à inclure les PI orphelines (rattachées à aucune StudyUnit) avec des parents {@code null}.
     */
    List<PhysicalInstanceSearchRow> getPhysicalInstanceSearchRows() {
        logger.info("Building physical instance advanced-search rows (bysubject descent Group -> StudyUnit -> PhysicalInstance)");
        String physicalInstanceType = itemType(PHYSICAL_INSTANCE);

        Map<String, PartialPhysicalInstance> piByKey = new LinkedHashMap<>();
        for (PartialPhysicalInstance pi : getPhysicalInstancesViaAdvancedQuery()) {
            piByKey.put(ColecticaItems.key(pi.agency(), pi.id()), pi);
        }

        List<PhysicalInstanceSearchRow> rows = new ArrayList<>();
        Set<String> attachedKeys = new HashSet<>();

        for (PartialGroup group : getGroups()) {
            List<ColecticaItem> studyUnits = colecticaClient.findRelatedItems(
                RelationshipDirection.BY_SUBJECT,
                new ItemReference(group.agency(), group.id()),
                List.of(STUDY_UNIT_UUID));
            for (ColecticaItem studyUnit : studyUnits) {
                String studyUnitLabel = labels.of(studyUnit);
                List<ItemReference> piRefs = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT,
                    ColecticaItems.itemRef(studyUnit),
                    List.of(physicalInstanceType));
                for (ItemReference piRef : piRefs) {
                    String piKey = ColecticaItems.key(piRef.agencyId(), piRef.identifier());
                    PartialPhysicalInstance pi = piByKey.get(piKey);
                    attachedKeys.add(piKey);
                    rows.add(new PhysicalInstanceSearchRow(
                        piRef.agencyId(), piRef.identifier(),
                        pi != null ? pi.label() : piRef.identifier(),
                        pi != null ? pi.versionDate() : null,
                        studyUnit.agencyId(), studyUnit.identifier(), studyUnitLabel,
                        group.agency(), group.id(), group.label()));
                }
            }
        }

        // PI orphelines (rattachées à aucune StudyUnit résolvable) : parents null.
        for (Map.Entry<String, PartialPhysicalInstance> entry : piByKey.entrySet()) {
            if (!attachedKeys.contains(entry.getKey())) {
                PartialPhysicalInstance pi = entry.getValue();
                rows.add(new PhysicalInstanceSearchRow(
                    pi.agency(), pi.id(), pi.label(), pi.versionDate(),
                    null, null, null, null, null, null));
            }
        }
        return rows;
    }

    /** Remontée {@code byobject} PhysicalInstance ← StudyUnit ← Group. */
    PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id) {
        ItemReference studyUnitItem = firstRelated(new ItemReference(agencyId, id), STUDY_UNIT_UUID)
            .orElseThrow(() -> new StudyUnitNotFoundException(
                "No study unit found for physical instance " + agencyId + "/" + id));

        ItemReference groupItem = firstRelated(
            new ItemReference(studyUnitItem.agencyId(), studyUnitItem.identifier()), GROUP_UUID)
            .orElseThrow(() -> new RuntimeException(
                "No group found for study unit "
                    + studyUnitItem.agencyId() + "/" + studyUnitItem.identifier()));

        return new PhysicalInstanceParents(
            studyUnitItem.agencyId(),
            studyUnitItem.identifier(),
            groupItem.agencyId(),
            groupItem.identifier());
    }

    private Optional<ItemReference> firstRelated(ItemReference from, String itemTypeUuid) {
        return colecticaClient.findRelatedDescriptions(
                RelationshipDirection.BY_OBJECT, from, List.of(itemTypeUuid))
            .stream()
            .findFirst();
    }

    /**
     * Le DDI 3.3 de la StudyUnit dont un {@code r:UserID} vaut {@code operationIri}, dans une unique
     * {@code <FragmentInstance>} : son fragment, suivi de ceux de ses PhysicalInstances (#1145).
     */
    Optional<String> findStudyUnitXmlByOperationIri(String operationIri) {
        return findStudyUnitFragmentsByOperationIri(operationIri)
            .map(fragments -> ColecticaXml.assembleFragmentInstance(fragments.fragmentXmls()));
    }

    /** Les mêmes fragments, projetés en DDI 4 pour la négociation JSON (#1145). */
    Optional<Ddi4StudyUnitResponse> findStudyUnitByOperationIri(String operationIri) {
        return findStudyUnitFragmentsByOperationIri(operationIri).map(this::toDdi4);
    }

    private Ddi4StudyUnitResponse toDdi4(StudyUnitFragments fragments) {
        ColecticaItemResponse item = fragments.studyUnit();
        Ddi4StudyUnit studyUnit = ddi3ToDdi4Converter.toStudyUnit(item.item());
        return new Ddi4StudyUnitResponse(
            Ddi4Response.SCHEMA,
            List.of(Reference.of(
                item.agencyId(), item.identifier(), String.valueOf(item.version()),
                Ddi4StudyUnit.TYPE)),
            List.of(studyUnit),
            toDdi4PhysicalInstances(fragments.physicalInstances()));
    }

    private List<Ddi4PhysicalInstance> toDdi4PhysicalInstances(
        List<ColecticaItemResponse> physicalInstances) {
        if (physicalInstances.isEmpty()) {
            return null;
        }
        List<Ddi3Response.Ddi3Item> ddi3Items = physicalInstances.stream()
            .map(ColecticaItems::toDdi3Item)
            .toList();
        Ddi4Response converted = ddi3ToDdi4Converter.convertDdi3ToDdi4(
            new Ddi3Response(null, ddi3Items), Ddi4Response.SCHEMA);
        return converted == null ? null : converted.physicalInstance();
    }

    private Optional<StudyUnitFragments> findStudyUnitFragmentsByOperationIri(String operationIri) {
        logger.info("Searching StudyUnit by operationIri: {}", operationIri);
        ColecticaResponse studyUnits = colecticaClient.query(List.of(STUDY_UNIT_UUID));
        List<GetDescriptionsRequest.IdentifierRef> identifiers =
            ColecticaItems.identifiersOf(studyUnits.results());
        if (identifiers.isEmpty()) {
            return Optional.empty();
        }
        // Un seul item/_getList pour tous les XML de StudyUnit, au lieu d'un appel HTTP par
        // StudyUnit — c'était la principale source de latence ici.
        ColecticaItemResponse[] items = colecticaClient.getDescriptions(identifiers);
        List<String> candidateUserIds = new ArrayList<>();
        for (ColecticaItemResponse item : items) {
            if (item == null) {
                continue;
            }
            List<String> userIds = ColecticaXml.userIds(item.item());
            candidateUserIds.addAll(userIds);
            if (userIds.contains(operationIri)) {
                return Optional.of(
                    new StudyUnitFragments(item, dereferencePhysicalInstances(item.item())));
            }
        }
        logger.warn(
            "No StudyUnit matched operationIri '{}' among {} study unit(s). Candidate UserIDs found: {}",
            operationIri, identifiers.size(), candidateUserIds);
        return Optional.empty();
    }

    /**
     * Les fragments des PhysicalInstances désignées par les {@code r:PhysicalInstanceReference} de la
     * StudyUnit, en un seul {@code item/_getList}. Une référence pendante est simplement absente de la
     * réponse de Colectica : elle est ignorée plutôt que de faire échouer toute la lecture.
     */
    private List<ColecticaItemResponse> dereferencePhysicalInstances(String studyUnitXml) {
        List<GetDescriptionsRequest.IdentifierRef> references =
            ColecticaXml.referencedIdentifiers(studyUnitXml, "PhysicalInstanceReference");
        if (references.isEmpty()) {
            return List.of();
        }
        ColecticaItemResponse[] items = colecticaClient.getDescriptions(references);
        if (items == null) {
            return List.of();
        }
        return Arrays.stream(items)
            .filter(Objects::nonNull)
            .filter(item -> item.item() != null)
            .toList();
    }

    String getItemXml(String agency, String id, String version) {
        ColecticaItemResponse response = colecticaClient.getItem(agency, id, version);
        return response != null ? response.item() : null;
    }

    private <T> List<T> queryPartials(String itemTypeUuid, PartialFactory<T> factory) {
        ColecticaResponse response = colecticaClient.query(List.of(itemTypeUuid));
        if (response == null || response.results() == null) {
            return List.of();
        }
        return response.results().stream()
            .map(item -> factory.create(
                item.identifier(),
                labels.of(item),
                ColecticaDates.parse(item.versionDate()),
                item.agencyId()))
            .toList();
    }

    private String itemType(String typeKey) {
        return instanceConfiguration.itemTypes().get(typeKey);
    }
}
