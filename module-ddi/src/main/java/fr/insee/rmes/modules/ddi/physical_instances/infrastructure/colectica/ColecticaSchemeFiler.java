package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CATEGORY_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.LOGICAL_PRODUCT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_REPRESENTATION_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE_SCHEME;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingSchemeException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Range les objets d'une PhysicalInstance sous les schemes de ses parents : listes de codes,
 * catégories et valeurs sentinelles sous les schemes du Group, variables sous le VariableScheme de la
 * StudyUnit.
 *
 * <p>Les LogicalProducts et leurs schemes sont créés en amont (init, miroir des opérations) : ce
 * rangement ne fait que fusionner des références dans des schemes existants. Un scheme introuvable
 * lève une {@link MissingSchemeException}, avant tout envoi à Colectica.
 *
 * <p>Chaque scheme mis à jour est ajouté au lot d'items à enregistrer, pour être écrit dans le même
 * batch atomique que la PhysicalInstance.
 */
class ColecticaSchemeFiler {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaSchemeFiler.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaConfiguration colecticaConfiguration;
    private final ColecticaClient colecticaClient;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;
    private final MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider;
    private final ColecticaCatalogRepository catalog;

    ColecticaSchemeFiler(
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaConfiguration colecticaConfiguration,
            ColecticaClient colecticaClient,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter,
            MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider,
            ColecticaCatalogRepository catalog) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaConfiguration = colecticaConfiguration;
        this.colecticaClient = colecticaClient;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.mutualizedCodeListRefsProvider = mutualizedCodeListRefsProvider;
        this.catalog = catalog;
    }

    /**
     * Les parents (PhysicalInstance → StudyUnit → Group) sont résolus une seule fois et seulement s'il
     * y a quelque chose à ranger, préservant le comportement pour les instances dont toutes les listes
     * de codes sont mutualisées (aucune résolution de parent, rien d'ajouté).
     *
     * @param knownParents les parents quand l'appelant les connaît déjà (le PATCH qui rattache
     *                     l'instance à une StudyUnit les porte dans sa requête) ; {@code null} pour les
     *                     résoudre via les relations Colectica
     * @throws MissingSchemeException quand un parent n'expose pas le scheme attendu
     */
    void appendSchemeUpdates(
            String agencyId,
            String id,
            Ddi4Response ddi4Response,
            List<ColecticaItemResponse> colecticaItems,
            PhysicalInstanceParents knownParents) {
        List<Ddi4CodeList> codeLists = ddi4Response.codeList();
        List<Ddi4CodeList> nonMutualized =
                (codeLists == null || codeLists.isEmpty()) ? List.of() : filterNonMutualizedCodeLists(codeLists);
        List<Ddi4Category> categories = orEmpty(ddi4Response.category());
        List<Ddi4Variable> variables = orEmpty(ddi4Response.variable());
        List<Ddi4ManagedMissingValuesRepresentation> missingValuesRepresentations =
                orEmpty(ddi4Response.managedMissingValuesRepresentation());

        boolean groupWork =
                !nonMutualized.isEmpty() || !categories.isEmpty() || !missingValuesRepresentations.isEmpty();
        boolean studyUnitWork = !variables.isEmpty();
        if (!groupWork && !studyUnitWork) {
            return;
        }

        PhysicalInstanceParents parents = knownParents;
        if (parents == null) {
            try {
                parents = catalog.getPhysicalInstanceParents(agencyId, id);
            } catch (StudyUnitNotFoundException _) {
                // Duplication étape 1 : le PUT brut part avant que l'instance ne soit rattachée à une
                // StudyUnit. On saute le rangement — le PATCH qui rattache l'instance réenregistre le
                // même contenu avec les parents portés par sa requête, et range tout à ce moment-là.
                logger.warn(
                        "Skipping scheme filing for physical instance {}/{}: no study unit attached yet", agencyId, id);
                return;
            }
        }
        if (groupWork) {
            appendGroupSchemesUpdate(parents, nonMutualized, categories, missingValuesRepresentations, colecticaItems);
        }
        if (studyUnitWork) {
            appendStudyUnitVariableSchemeUpdate(parents, variables, colecticaItems);
        }
    }

    /**
     * Range les listes de codes non mutualisées, les catégories et les valeurs sentinelles sous les
     * schemes atteignables via {@code Group → LogicalProduct → scheme}.
     */
    private void appendGroupSchemesUpdate(
            PhysicalInstanceParents parents,
            List<Ddi4CodeList> nonMutualized,
            List<Ddi4Category> categories,
            List<Ddi4ManagedMissingValuesRepresentation> missingValuesRepresentations,
            List<ColecticaItemResponse> colecticaItems) {
        String groupAgency = parents.groupAgency();
        String groupId = parents.groupId();
        List<ItemReference> logicalProducts = findContainerLogicalProducts(groupAgency, groupId);

        if (!nonMutualized.isEmpty()) {
            fileGroupCodeLists(groupAgency, groupId, logicalProducts, nonMutualized, colecticaItems);
        }
        if (!categories.isEmpty()) {
            fileGroupCategories(groupAgency, groupId, logicalProducts, categories, colecticaItems);
        }
        if (!missingValuesRepresentations.isEmpty()) {
            fileGroupManagedMissingValues(
                    groupAgency, groupId, logicalProducts, missingValuesRepresentations, colecticaItems);
        }
    }

    private void fileGroupCodeLists(
            String groupAgency,
            String groupId,
            List<ItemReference> logicalProducts,
            List<Ddi4CodeList> nonMutualized,
            List<ColecticaItemResponse> colecticaItems) {
        List<Reference> newRefs = nonMutualized.stream()
                .map(cl -> Reference.of(cl.agency(), cl.id(), cl.version(), Ddi4CodeList.TYPE))
                .toList();
        ItemReference schemeRef = requireGroupScheme(
                groupAgency,
                groupId,
                logicalProducts,
                CODE_LIST_SCHEME,
                MissingSchemeException.Code.GROUP_MISSING_CODE_LIST_SCHEME,
                "ses listes de codes");
        Ddi4CodeListScheme current = ddi3ToDdi4Converter.toCodeListScheme(itemXml(schemeRef));
        List<Reference> merged = mergeReferences(current.codeListReference(), newRefs);
        if (merged == null) {
            return;
        }
        Ddi4CodeListScheme updated = new Ddi4CodeListScheme(
                current.type(),
                current.versionDate(),
                current.urn(),
                current.agency(),
                current.id(),
                current.version(),
                current.label(),
                merged);
        colecticaItems.add(ColecticaItems.toColecticaItem(ddi4ToDdi3Converter.toCodeListSchemeItem(updated)));
        logger.info(
                "Filed {} code list(s) under code list scheme {}/{} of group {}/{}",
                newRefs.size(),
                schemeRef.agencyId(),
                schemeRef.identifier(),
                groupAgency,
                groupId);
    }

    private void fileGroupCategories(
            String groupAgency,
            String groupId,
            List<ItemReference> logicalProducts,
            List<Ddi4Category> categories,
            List<ColecticaItemResponse> colecticaItems) {
        List<Reference> newRefs = categories.stream()
                .map(cat -> Reference.of(cat.agency(), cat.id(), cat.version(), Ddi4Category.TYPE))
                .toList();
        ItemReference schemeRef = requireGroupScheme(
                groupAgency,
                groupId,
                logicalProducts,
                CATEGORY_SCHEME,
                MissingSchemeException.Code.GROUP_MISSING_CATEGORY_SCHEME,
                "ses catégories");
        Ddi4CategoryScheme current = ddi3ToDdi4Converter.toCategoryScheme(itemXml(schemeRef));
        List<Reference> merged = mergeReferences(current.categoryReference(), newRefs);
        if (merged == null) {
            return;
        }
        Ddi4CategoryScheme updated = new Ddi4CategoryScheme(
                current.type(),
                current.versionDate(),
                current.urn(),
                current.agency(),
                current.id(),
                current.version(),
                current.label(),
                merged);
        colecticaItems.add(ColecticaItems.toColecticaItem(ddi4ToDdi3Converter.toCategorySchemeItem(updated)));
        logger.info(
                "Filed {} category(ies) under category scheme {}/{} of group {}/{}",
                newRefs.size(),
                schemeRef.agencyId(),
                schemeRef.identifier(),
                groupAgency,
                groupId);
    }

    /**
     * Range les ManagedMissingValuesRepresentations du groupe (valeurs sentinelles, #1566) sous son
     * ManagedRepresentationScheme, en miroir de {@link #fileGroupCodeLists}.
     */
    private void fileGroupManagedMissingValues(
            String groupAgency,
            String groupId,
            List<ItemReference> logicalProducts,
            List<Ddi4ManagedMissingValuesRepresentation> missingValuesRepresentations,
            List<ColecticaItemResponse> colecticaItems) {
        List<Reference> newRefs = missingValuesRepresentations.stream()
                .map(mmvr -> Reference.of(
                        mmvr.agency(), mmvr.id(), mmvr.version(), Ddi4ManagedMissingValuesRepresentation.TYPE))
                .toList();
        ItemReference schemeRef = requireGroupScheme(
                groupAgency,
                groupId,
                logicalProducts,
                MANAGED_REPRESENTATION_SCHEME,
                MissingSchemeException.Code.GROUP_MISSING_MANAGED_REPRESENTATION_SCHEME,
                "ses valeurs sentinelles");
        Ddi4ManagedRepresentationScheme current = ddi3ToDdi4Converter.toManagedRepresentationScheme(itemXml(schemeRef));
        List<Reference> merged = mergeReferences(current.managedRepresentationReference(), newRefs);
        if (merged == null) {
            return;
        }
        Ddi4ManagedRepresentationScheme updated = new Ddi4ManagedRepresentationScheme(
                current.type(),
                current.versionDate(),
                current.urn(),
                current.agency(),
                current.id(),
                current.version(),
                current.label(),
                merged);
        colecticaItems.add(
                ColecticaItems.toColecticaItem(ddi4ToDdi3Converter.toManagedRepresentationSchemeItem(updated)));
        logger.info(
                "Filed {} missing values representation(s) under managed representation scheme "
                        + "{}/{} of group {}/{}",
                newRefs.size(),
                schemeRef.agencyId(),
                schemeRef.identifier(),
                groupAgency,
                groupId);
    }

    private ItemReference requireGroupScheme(
            String groupAgency,
            String groupId,
            List<ItemReference> logicalProducts,
            String schemeTypeKey,
            MissingSchemeException.Code code,
            String filedObjects) {
        return findScheme(logicalProducts, schemeTypeKey)
                .orElseThrow(() -> new MissingSchemeException(
                        code,
                        Map.of("group", groupAgency + "/" + groupId),
                        "La série (Group %s/%s) n'a pas de %s pour ranger %s : il doit être créé en amont."
                                .formatted(groupAgency, groupId, schemeTypeKey, filedObjects)));
    }

    /**
     * Range les variables de la PhysicalInstance sous le VariableScheme de sa StudyUnit, atteint via
     * {@code StudyUnit → LogicalProduct → VariableScheme}. La StudyUnit doit exposer exactement un
     * LogicalProduct : c'est lui, et lui seul, qui porte ses variables.
     */
    private void appendStudyUnitVariableSchemeUpdate(
            PhysicalInstanceParents parents, List<Ddi4Variable> variables, List<ColecticaItemResponse> colecticaItems) {
        String suAgency = parents.studyUnitAgency();
        String suId = parents.studyUnitId();
        List<Reference> newRefs = variables.stream()
                .map(v -> Reference.of(v.agency(), v.id(), v.version(), Ddi4Variable.TYPE))
                .toList();

        List<ItemReference> logicalProducts = findContainerLogicalProducts(suAgency, suId);
        String studyUnit = suAgency + "/" + suId;
        if (logicalProducts.isEmpty()) {
            throw new MissingSchemeException(
                    MissingSchemeException.Code.STUDY_UNIT_MISSING_LOGICAL_PRODUCT,
                    Map.of("studyUnit", studyUnit),
                    "L'opération (StudyUnit %s/%s) n'a pas de LogicalProduct pour ranger ses variables : il doit être créé en amont."
                            .formatted(suAgency, suId));
        }
        if (logicalProducts.size() > 1) {
            throw new MissingSchemeException(
                    MissingSchemeException.Code.STUDY_UNIT_SEVERAL_LOGICAL_PRODUCTS,
                    Map.of("studyUnit", studyUnit, "count", String.valueOf(logicalProducts.size())),
                    "L'opération (StudyUnit %s/%s) a %d LogicalProducts : un seul est attendu pour ranger ses variables."
                            .formatted(suAgency, suId, logicalProducts.size()));
        }
        ItemReference schemeRef = findScheme(logicalProducts, VARIABLE_SCHEME)
                .orElseThrow(() -> new MissingSchemeException(
                        MissingSchemeException.Code.STUDY_UNIT_MISSING_VARIABLE_SCHEME,
                        Map.of("studyUnit", studyUnit),
                        "L'opération (StudyUnit %s/%s) n'a pas de VariableScheme pour ranger ses variables : il doit être créé en amont."
                                .formatted(suAgency, suId)));

        Ddi4VariableScheme current = ddi3ToDdi4Converter.toVariableScheme(itemXml(schemeRef));
        List<Reference> merged = mergeReferences(current.variableReference(), newRefs);
        if (merged == null) {
            return;
        }
        Ddi4VariableScheme updated = new Ddi4VariableScheme(
                current.type(),
                current.versionDate(),
                current.urn(),
                current.agency(),
                current.id(),
                current.version(),
                current.label(),
                merged);
        colecticaItems.add(ColecticaItems.toColecticaItem(ddi4ToDdi3Converter.toVariableSchemeItem(updated)));
        logger.info(
                "Filed {} variable(s) under variable scheme {}/{} of study unit {}/{}",
                newRefs.size(),
                schemeRef.agencyId(),
                schemeRef.identifier(),
                suAgency,
                suId);
    }

    /**
     * Ne garde que les listes de codes non mutualisées, c'est-à-dire hors de l'arbre du package de
     * codes mutualisés configuré. Sans package configuré, toutes les listes sont considérées non
     * mutualisées.
     *
     * <p>L'appartenance est testée contre l'ensemble (mis en cache) des CodeLists collectées par
     * descente du package — la même source de vérité que le catalogue mutualisé — de sorte qu'une
     * unique descente bornée remplace la remontée de parents par liste de codes, et qu'un appel à
     * chaud ne coûte aucun HTTP.
     */
    private List<Ddi4CodeList> filterNonMutualizedCodeLists(List<Ddi4CodeList> codeLists) {
        if (colecticaConfiguration.mutualizedCodesPackage() == null) {
            return codeLists;
        }
        Set<String> mutualizedKeys = mutualizedCodeListRefsProvider.codeListRefs().stream()
                .map(ref -> ref.agencyId() + "/" + ref.identifier())
                .collect(Collectors.toSet());
        return codeLists.stream()
                .filter(cl -> !mutualizedKeys.contains(cl.agency() + "/" + cl.id()))
                .toList();
    }

    /** Les LogicalProducts rangés par un conteneur (Group ou StudyUnit), via {@code bysubject}. */
    private List<ItemReference> findContainerLogicalProducts(String containerAgency, String containerId) {
        return ColecticaRelationships.childrenOfType(
                colecticaClient, new ItemReference(containerAgency, containerId), itemType(LOGICAL_PRODUCT));
    }

    /** Cherche un scheme rangé sous l'un des {@code logicalProducts}. Vide quand il n'en existe pas. */
    private Optional<ItemReference> findScheme(List<ItemReference> logicalProducts, String schemeTypeKey) {
        String schemeType = itemType(schemeTypeKey);
        for (ItemReference logicalProduct : logicalProducts) {
            Optional<ItemReference> scheme = colecticaClient
                    .findRelatedDescriptions(RelationshipDirection.BY_SUBJECT, logicalProduct, List.of(schemeType))
                    .stream()
                    .findFirst();
            if (scheme.isPresent()) {
                return scheme;
            }
        }
        return Optional.empty();
    }

    /**
     * Fusionne {@code toAdd} dans {@code existing} (dédoublonné par {@code agence/id}), en préservant
     * les références déjà présentes. Renvoie la liste fusionnée, ou {@code null} quand rien de nouveau
     * n'a été ajouté (l'appelant peut alors éviter de réenregistrer un scheme inchangé).
     */
    private List<Reference> mergeReferences(List<Reference> existing, List<Reference> toAdd) {
        List<Reference> merged = new ArrayList<>(orEmpty(existing));
        Set<String> keys = merged.stream().map(r -> r.agency() + "/" + r.id()).collect(Collectors.toSet());
        boolean changed = false;
        for (Reference ref : toAdd) {
            if (keys.add(ref.agency() + "/" + ref.id())) {
                merged.add(ref);
                changed = true;
            }
        }
        return changed ? merged : null;
    }

    private String itemXml(ItemReference ref) {
        return colecticaClient.getItem(ref.agencyId(), ref.identifier(), null).item();
    }

    private String itemType(String typeKey) {
        return instanceConfiguration.itemTypes().get(typeKey);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list != null ? list : List.of();
    }
}
