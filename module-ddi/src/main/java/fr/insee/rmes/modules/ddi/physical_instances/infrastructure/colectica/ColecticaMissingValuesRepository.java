package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.LOGICAL_PRODUCT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_MISSING_VALUES_REPRESENTATION;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_REPRESENTATION_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingValuesRepresentationInUseException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ValueType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Valeurs sentinelles réutilisables d'un groupe (#1566) : les ManagedMissingValuesRepresentations
 * rangées dans les ManagedRepresentationSchemes de ses LogicalProducts, et leur suppression.
 */
class ColecticaMissingValuesRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaMissingValuesRepository.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;
    private final ColecticaHierarchyBrowser hierarchy;

    ColecticaMissingValuesRepository(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaClient colecticaClient,
        DDI3toDDI4ConverterService ddi3ToDdi4Converter,
        DDI4toDDI3ConverterService ddi4ToDdi3Converter,
        ColecticaHierarchyBrowser hierarchy
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.hierarchy = hierarchy;
    }

    /**
     * Même descente que {@code getMissingCodesListsByGroup} mais arrêtée au niveau MMVR ; le fragment
     * de chaque MMVR est ensuite récupéré et parsé pour son libellé, et la CodeList de sentinelles
     * qu'elle référence est chargée une fois pour construire l'aperçu de codes affiché par le
     * sélecteur de réutilisation.
     */
    List<PartialMissingValuesRepresentation> getMissingValuesRepresentationsByGroup(
        String agencyId, String groupId
    ) {
        logger.info("Fetching reusable missing values representations for group {}/{}", agencyId, groupId);
        List<ItemReference> refs = hierarchy.descendFromGroup(agencyId, groupId, List.of(
            LOGICAL_PRODUCT, MANAGED_REPRESENTATION_SCHEME, MANAGED_MISSING_VALUES_REPRESENTATION));
        if (refs.isEmpty()) {
            return List.of();
        }

        Map<String, List<String>> codeValuesByCodeListKey = new HashMap<>();
        List<PartialMissingValuesRepresentation> result = new ArrayList<>();
        for (ItemReference ref : refs) {
            Ddi4ManagedMissingValuesRepresentation mmvr = readMissingValues(ref.agencyId(), ref.identifier());
            Reference codeListRef = firstSentinelCodeListReference(mmvr);
            result.add(new PartialMissingValuesRepresentation(
                mmvr.id(),
                mmvr.agency(),
                mmvr.version(),
                ColecticaLabels.firstValue(mmvr.label()),
                codeListRef != null ? codeListRef.id() : null,
                codeListRef != null
                    ? codeValuesByCodeListKey.computeIfAbsent(
                        codeListRef.agency() + "/" + codeListRef.id(),
                        _ -> sentinelCodeValues(codeListRef))
                    : List.of()));
        }
        return result;
    }

    /**
     * Supprime une ManagedMissingValuesRepresentation orpheline (#1566) : refuse si une variable la
     * référence encore, sinon la défile du ManagedRepresentationScheme du groupe (et sa CodeList de
     * sentinelles du CodeListScheme), puis supprime la MMVR, la CodeList et les catégories de celle-ci.
     */
    void deleteMissingValuesRepresentation(String agencyId, String mmvrId) {
        logger.info("Deleting missing values representation {}/{}", agencyId, mmvrId);
        ItemReference mmvrRef = new ItemReference(agencyId, mmvrId);

        // Refus au premier niveau : toute Variable référençant la MMVR bloque la suppression,
        // même si sa chaîne DataRelationship/PhysicalInstance n'est pas résoluble.
        List<ColecticaItem> referencingVariables = colecticaClient.findRelatedItems(
            RelationshipDirection.BY_OBJECT, mmvrRef, List.of(itemType(VARIABLE)));
        if (!referencingVariables.isEmpty()) {
            throw new MissingValuesRepresentationInUseException(
                "La liste de valeurs sentinelles %s/%s est encore utilisée par %d variable(s)"
                    .formatted(agencyId, mmvrId, referencingVariables.size()));
        }

        Reference sentinelCodeListRef =
            firstSentinelCodeListReference(readMissingValues(agencyId, mmvrId));

        // Défilage : re-registre les schemes du groupe sans les références supprimées.
        List<ColecticaItemResponse> updatedSchemes = new ArrayList<>();
        for (ItemReference schemeRef : referencedBy(mmvrRef, MANAGED_REPRESENTATION_SCHEME)) {
            Ddi4ManagedRepresentationScheme scheme =
                ddi3ToDdi4Converter.toManagedRepresentationScheme(itemXml(schemeRef));
            Ddi4ManagedRepresentationScheme updated = new Ddi4ManagedRepresentationScheme(
                scheme.type(), scheme.versionDate(), scheme.urn(), scheme.agency(), scheme.id(),
                scheme.version(), scheme.label(),
                withoutReference(scheme.managedRepresentationReference(), agencyId, mmvrId));
            updatedSchemes.add(ColecticaItems.toColecticaItem(
                ddi4ToDdi3Converter.toManagedRepresentationSchemeItem(updated)));
        }

        List<Reference> categoryRefs = List.of();
        if (sentinelCodeListRef != null) {
            ItemReference codeListRef =
                new ItemReference(sentinelCodeListRef.agency(), sentinelCodeListRef.id());
            for (ItemReference schemeRef : referencedBy(codeListRef, CODE_LIST_SCHEME)) {
                Ddi4CodeListScheme scheme = ddi3ToDdi4Converter.toCodeListScheme(itemXml(schemeRef));
                Ddi4CodeListScheme updated = new Ddi4CodeListScheme(
                    scheme.type(), scheme.versionDate(), scheme.urn(), scheme.agency(), scheme.id(),
                    scheme.version(), scheme.label(),
                    withoutReference(scheme.codeListReference(),
                        sentinelCodeListRef.agency(), sentinelCodeListRef.id()));
                updatedSchemes.add(ColecticaItems.toColecticaItem(
                    ddi4ToDdi3Converter.toCodeListSchemeItem(updated)));
            }
            Ddi4CodeList sentinelCodeList = readCodeList(codeListRef);
            categoryRefs = codesOf(sentinelCodeList).stream()
                .map(Code::categoryReference)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        }

        if (!updatedSchemes.isEmpty()) {
            colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(updatedSchemes));
        }
        colecticaClient.deleteItem(agencyId, mmvrId);
        if (sentinelCodeListRef != null) {
            colecticaClient.deleteItem(sentinelCodeListRef.agency(), sentinelCodeListRef.id());
            for (Reference categoryRef : categoryRefs) {
                colecticaClient.deleteItem(categoryRef.agency(), categoryRef.id());
            }
        }
        logger.info("Deleted missing values representation {}/{} (code list: {})",
            agencyId, mmvrId, sentinelCodeListRef != null ? sentinelCodeListRef.id() : "none");
    }

    private Reference firstSentinelCodeListReference(Ddi4ManagedMissingValuesRepresentation mmvr) {
        if (mmvr.missingCodeRepresentation() == null) {
            return null;
        }
        return mmvr.missingCodeRepresentation().stream()
            .map(CodeRepresentation::codeListReference)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private List<String> sentinelCodeValues(Reference codeListRef) {
        Ddi4CodeList codeList = readCodeList(new ItemReference(codeListRef.agency(), codeListRef.id()));
        return codesOf(codeList).stream()
            .map(Code::value)
            .filter(Objects::nonNull)
            .map(ValueType::stringValue)
            .toList();
    }

    private static List<Code> codesOf(Ddi4CodeList codeList) {
        return codeList.code() != null ? codeList.code() : List.of();
    }

    private static List<Reference> withoutReference(
        List<Reference> references, String agency, String id
    ) {
        List<Reference> kept = (references != null ? references : List.<Reference>of()).stream()
            .filter(ref -> !(agency.equals(ref.agency()) && id.equals(ref.id())))
            .toList();
        return kept.isEmpty() ? null : kept;
    }

    private List<ItemReference> referencedBy(ItemReference item, String parentTypeKey) {
        return colecticaClient.findRelatedDescriptions(
            RelationshipDirection.BY_OBJECT, item, List.of(itemType(parentTypeKey)));
    }

    private Ddi4ManagedMissingValuesRepresentation readMissingValues(String agencyId, String id) {
        return ddi3ToDdi4Converter.toManagedMissingValuesRepresentation(
            colecticaClient.getItem(agencyId, id, null).item());
    }

    private Ddi4CodeList readCodeList(ItemReference ref) {
        return ddi3ToDdi4Converter.toCodeList(itemXml(ref));
    }

    private String itemXml(ItemReference ref) {
        return colecticaClient.getItem(ref.agencyId(), ref.identifier(), null).item();
    }

    private String itemType(String typeKey) {
        return instanceConfiguration.itemTypes().get(typeKey);
    }
}
