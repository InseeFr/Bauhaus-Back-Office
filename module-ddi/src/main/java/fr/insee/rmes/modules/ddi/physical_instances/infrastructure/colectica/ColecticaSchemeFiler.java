package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CATEGORY_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.LOGICAL_PRODUCT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_REPRESENTATION_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.STUDY_UNIT_UUID;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE_SCHEME;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Range les objets d'une PhysicalInstance sous les schemes de ses parents : listes de codes,
 * catégories et valeurs sentinelles sous les schemes du Group, variables sous le VariableScheme de la
 * StudyUnit — en auto-provisionnant tout scheme (et son LogicalProduct) qui n'existe pas encore.
 *
 * <p>Chaque item produit est ajouté au lot d'items à enregistrer, pour être écrit dans le même batch
 * atomique que la PhysicalInstance.
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
    private final String defaultLang;

    ColecticaSchemeFiler(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaConfiguration colecticaConfiguration,
        ColecticaClient colecticaClient,
        DDI3toDDI4ConverterService ddi3ToDdi4Converter,
        DDI4toDDI3ConverterService ddi4ToDdi3Converter,
        MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider,
        ColecticaCatalogRepository catalog,
        String defaultLang
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaConfiguration = colecticaConfiguration;
        this.colecticaClient = colecticaClient;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.mutualizedCodeListRefsProvider = mutualizedCodeListRefsProvider;
        this.catalog = catalog;
        this.defaultLang = defaultLang;
    }

    /**
     * Les parents (PhysicalInstance → StudyUnit → Group) sont résolus une seule fois et seulement s'il
     * y a quelque chose à ranger, préservant le comportement pour les instances dont toutes les listes
     * de codes sont mutualisées (aucune résolution de parent, rien d'ajouté).
     *
     * @param knownParents les parents quand l'appelant les connaît déjà (le PATCH qui rattache
     *                     l'instance à une StudyUnit les porte dans sa requête) ; {@code null} pour les
     *                     résoudre via les relations Colectica
     */
    void appendSchemeUpdates(
        String agencyId,
        String id,
        Ddi4Response ddi4Response,
        List<ColecticaItemResponse> colecticaItems,
        List<ColecticaItemResponse> additionalItems,
        PhysicalInstanceParents knownParents
    ) {
        List<Ddi4CodeList> codeLists = ddi4Response.codeList();
        List<Ddi4CodeList> nonMutualized = (codeLists == null || codeLists.isEmpty())
            ? List.of() : filterNonMutualizedCodeLists(codeLists);
        List<Ddi4Category> categories = orEmpty(ddi4Response.category());
        List<Ddi4Variable> variables = orEmpty(ddi4Response.variable());
        List<Ddi4ManagedMissingValuesRepresentation> missingValuesRepresentations =
            orEmpty(ddi4Response.managedMissingValuesRepresentation());

        boolean groupWork = !nonMutualized.isEmpty() || !categories.isEmpty()
            || !missingValuesRepresentations.isEmpty();
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
                    "Skipping scheme filing for physical instance {}/{}: no study unit attached yet",
                    agencyId, id);
                return;
            }
        }
        if (groupWork) {
            appendGroupSchemesUpdate(parents, nonMutualized, categories, missingValuesRepresentations,
                colecticaItems);
        }
        if (studyUnitWork) {
            appendStudyUnitVariableSchemeUpdate(parents, variables, colecticaItems, additionalItems);
        }
    }

    /**
     * Range les listes de codes non mutualisées (sous le CodeListScheme du groupe) et les catégories
     * (sous son CategoryScheme) : pour chaque scheme atteignable via {@code Group → LogicalProduct →
     * scheme}, les nouvelles références y sont fusionnées ; quand le scheme n'existe pas encore, il est
     * créé.
     *
     * <p>Tout scheme créé ici est rangé sous un <em>seul</em> LogicalProduct — celui que le groupe
     * expose déjà, ou un unique LogicalProduct neuf — afin qu'un groupe ne se retrouve jamais avec un
     * CodeListScheme et un CategoryScheme dans deux branches {@code Group → LogicalProduct}
     * différentes. Le groupe est réenregistré au plus une fois, et seulement si ce LogicalProduct est neuf.
     */
    private void appendGroupSchemesUpdate(
        PhysicalInstanceParents parents,
        List<Ddi4CodeList> nonMutualized,
        List<Ddi4Category> categories,
        List<Ddi4ManagedMissingValuesRepresentation> missingValuesRepresentations,
        List<ColecticaItemResponse> colecticaItems
    ) {
        String groupAgency = parents.groupAgency();
        String groupId = parents.groupId();
        List<ItemReference> logicalProducts = findContainerLogicalProducts(groupAgency, groupId);
        List<Reference> newSchemeRefs = new ArrayList<>();

        if (!nonMutualized.isEmpty()) {
            fileGroupCodeLists(groupAgency, groupId, logicalProducts, nonMutualized, colecticaItems)
                .ifPresent(newSchemeRefs::add);
        }
        if (!categories.isEmpty()) {
            fileGroupCategories(groupAgency, groupId, logicalProducts, categories, colecticaItems)
                .ifPresent(newSchemeRefs::add);
        }
        if (!missingValuesRepresentations.isEmpty()) {
            fileGroupManagedMissingValues(groupAgency, groupId, logicalProducts,
                missingValuesRepresentations, colecticaItems)
                .ifPresent(newSchemeRefs::add);
        }
        if (!newSchemeRefs.isEmpty()) {
            fileSchemesUnderGroupLogicalProduct(
                groupAgency, groupId, logicalProducts, newSchemeRefs, colecticaItems);
        }
    }

    /**
     * @return une référence vers le CodeListScheme qu'il a fallu créer (à ranger sous le LogicalProduct
     *         du groupe), ou vide quand les listes ont été fusionnées dans un scheme existant
     */
    private Optional<Reference> fileGroupCodeLists(
        String groupAgency, String groupId, List<ItemReference> logicalProducts,
        List<Ddi4CodeList> nonMutualized, List<ColecticaItemResponse> colecticaItems
    ) {
        List<Reference> newRefs = nonMutualized.stream()
            .map(cl -> Reference.of(cl.agency(), cl.id(), cl.version(), Ddi4CodeList.TYPE))
            .toList();
        Optional<ItemReference> schemeRefOpt = findScheme(logicalProducts, CODE_LIST_SCHEME);
        if (schemeRefOpt.isPresent()) {
            ItemReference schemeRef = schemeRefOpt.get();
            Ddi4CodeListScheme current = ddi3ToDdi4Converter.toCodeListScheme(itemXml(schemeRef));
            List<Reference> merged = mergeReferences(current.codeListReference(), newRefs);
            if (merged == null) {
                return Optional.empty();
            }
            Ddi4CodeListScheme updated = new Ddi4CodeListScheme(current.type(), current.versionDate(),
                current.urn(), current.agency(), current.id(), current.version(), current.label(), merged);
            colecticaItems.add(ColecticaItems.toColecticaItem(
                ddi4ToDdi3Converter.toCodeListSchemeItem(updated)));
            logger.info("Filed {} code list(s) under code list scheme {}/{} of group {}/{}",
                newRefs.size(), schemeRef.agencyId(), schemeRef.identifier(), groupAgency, groupId);
            return Optional.empty();
        }
        String schemeId = deterministicId(groupAgency, groupId, "#codelistscheme");
        Ddi4CodeListScheme scheme = new Ddi4CodeListScheme(Ddi4CodeListScheme.TYPE,
            CogsDate.ofDateTime(ColecticaDates.nowIso()), urn(groupAgency, schemeId),
            groupAgency, schemeId, "1", LangStrings.of(defaultLang, "Code List Scheme"), newRefs);
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toCodeListSchemeItem(scheme)));
        logger.info("Auto-provisioned code list scheme {}/{} for group {}/{} and filed {} code list(s)",
            groupAgency, schemeId, groupAgency, groupId, newRefs.size());
        return Optional.of(Reference.of(groupAgency, schemeId, "1", CODE_LIST_SCHEME));
    }

    /**
     * @return une référence vers le CategoryScheme qu'il a fallu créer (à ranger sous le LogicalProduct
     *         du groupe), ou vide quand les catégories ont été fusionnées dans un scheme existant
     */
    private Optional<Reference> fileGroupCategories(
        String groupAgency, String groupId, List<ItemReference> logicalProducts,
        List<Ddi4Category> categories, List<ColecticaItemResponse> colecticaItems
    ) {
        List<Reference> newRefs = categories.stream()
            .map(cat -> Reference.of(cat.agency(), cat.id(), cat.version(), Ddi4Category.TYPE))
            .toList();
        Optional<ItemReference> schemeRefOpt = findScheme(logicalProducts, CATEGORY_SCHEME);
        if (schemeRefOpt.isPresent()) {
            ItemReference schemeRef = schemeRefOpt.get();
            Ddi4CategoryScheme current = ddi3ToDdi4Converter.toCategoryScheme(itemXml(schemeRef));
            List<Reference> merged = mergeReferences(current.categoryReference(), newRefs);
            if (merged == null) {
                return Optional.empty();
            }
            Ddi4CategoryScheme updated = new Ddi4CategoryScheme(current.type(), current.versionDate(),
                current.urn(), current.agency(), current.id(), current.version(), current.label(), merged);
            colecticaItems.add(ColecticaItems.toColecticaItem(
                ddi4ToDdi3Converter.toCategorySchemeItem(updated)));
            logger.info("Filed {} category(ies) under category scheme {}/{} of group {}/{}",
                newRefs.size(), schemeRef.agencyId(), schemeRef.identifier(), groupAgency, groupId);
            return Optional.empty();
        }
        String schemeId = deterministicId(groupAgency, groupId, "#categoryscheme");
        Ddi4CategoryScheme scheme = new Ddi4CategoryScheme(Ddi4CategoryScheme.TYPE,
            CogsDate.ofDateTime(ColecticaDates.nowIso()), urn(groupAgency, schemeId),
            groupAgency, schemeId, "1", LangStrings.of(defaultLang, "Category Scheme"), newRefs);
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toCategorySchemeItem(scheme)));
        logger.info("Auto-provisioned category scheme {}/{} for group {}/{} and filed {} category(ies)",
            groupAgency, schemeId, groupAgency, groupId, newRefs.size());
        return Optional.of(Reference.of(groupAgency, schemeId, "1", CATEGORY_SCHEME));
    }

    /**
     * Range les ManagedMissingValuesRepresentations du groupe (valeurs sentinelles, #1566) sous son
     * ManagedRepresentationScheme, en miroir de {@link #fileGroupCodeLists}.
     *
     * @return une référence vers le ManagedRepresentationScheme qu'il a fallu créer (à ranger sous le
     *         LogicalProduct du groupe), ou vide quand les références ont été fusionnées dans un
     *         scheme existant
     */
    private Optional<Reference> fileGroupManagedMissingValues(
        String groupAgency, String groupId, List<ItemReference> logicalProducts,
        List<Ddi4ManagedMissingValuesRepresentation> missingValuesRepresentations,
        List<ColecticaItemResponse> colecticaItems
    ) {
        List<Reference> newRefs = missingValuesRepresentations.stream()
            .map(mmvr -> Reference.of(mmvr.agency(), mmvr.id(), mmvr.version(),
                Ddi4ManagedMissingValuesRepresentation.TYPE))
            .toList();
        Optional<ItemReference> schemeRefOpt = findScheme(logicalProducts, MANAGED_REPRESENTATION_SCHEME);
        if (schemeRefOpt.isPresent()) {
            ItemReference schemeRef = schemeRefOpt.get();
            Ddi4ManagedRepresentationScheme current =
                ddi3ToDdi4Converter.toManagedRepresentationScheme(itemXml(schemeRef));
            List<Reference> merged = mergeReferences(current.managedRepresentationReference(), newRefs);
            if (merged == null) {
                return Optional.empty();
            }
            Ddi4ManagedRepresentationScheme updated = new Ddi4ManagedRepresentationScheme(
                current.type(), current.versionDate(), current.urn(), current.agency(), current.id(),
                current.version(), current.label(), merged);
            colecticaItems.add(ColecticaItems.toColecticaItem(
                ddi4ToDdi3Converter.toManagedRepresentationSchemeItem(updated)));
            logger.info("Filed {} missing values representation(s) under managed representation scheme "
                    + "{}/{} of group {}/{}",
                newRefs.size(), schemeRef.agencyId(), schemeRef.identifier(), groupAgency, groupId);
            return Optional.empty();
        }
        String schemeId = deterministicId(groupAgency, groupId, "#managedrepresentationscheme");
        Ddi4ManagedRepresentationScheme scheme = new Ddi4ManagedRepresentationScheme(
            Ddi4ManagedRepresentationScheme.TYPE,
            CogsDate.ofDateTime(ColecticaDates.nowIso()), urn(groupAgency, schemeId),
            groupAgency, schemeId, "1", LangStrings.of(defaultLang, "Managed Representation Scheme"),
            newRefs);
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toManagedRepresentationSchemeItem(scheme)));
        logger.info("Auto-provisioned managed representation scheme {}/{} for group {}/{} and filed "
                + "{} missing values representation(s)",
            groupAgency, schemeId, groupAgency, groupId, newRefs.size());
        return Optional.of(Reference.of(groupAgency, schemeId, "1", MANAGED_REPRESENTATION_SCHEME));
    }

    /**
     * Range les schemes qui viennent d'être créés sous le LogicalProduct du groupe : celui que le
     * groupe expose déjà est complété avec les nouvelles références de scheme (le groupe pointe déjà
     * dessus, il n'est donc pas réenregistré) ; sinon un unique LogicalProduct neuf portant tous les
     * nouveaux schemes est créé et le groupe est réenregistré en pointant dessus.
     */
    private void fileSchemesUnderGroupLogicalProduct(
        String groupAgency, String groupId, List<ItemReference> logicalProducts,
        List<Reference> newSchemeRefs, List<ColecticaItemResponse> colecticaItems
    ) {
        if (!logicalProducts.isEmpty()) {
            ItemReference logicalProductRef = logicalProducts.getFirst();
            Ddi4LogicalProduct current = ddi3ToDdi4Converter.toLogicalProduct(itemXml(logicalProductRef));
            Ddi4LogicalProduct updated = new Ddi4LogicalProduct(
                current.type(), current.versionDate(), current.urn(), current.agency(), current.id(),
                current.version(), current.label(),
                addSchemeReferences(current.codeListSchemeReference(), newSchemeRefs, CODE_LIST_SCHEME),
                addSchemeReferences(current.categorySchemeReference(), newSchemeRefs, CATEGORY_SCHEME),
                current.variableSchemeReference(),
                addSchemeReferences(current.managedRepresentationSchemeReference(), newSchemeRefs,
                    MANAGED_REPRESENTATION_SCHEME));
            colecticaItems.add(ColecticaItems.toColecticaItem(
                ddi4ToDdi3Converter.toLogicalProductItem(updated)));
            logger.info("Filed {} scheme(s) under the existing logical product {}/{} of group {}/{}",
                newSchemeRefs.size(), logicalProductRef.agencyId(), logicalProductRef.identifier(),
                groupAgency, groupId);
            return;
        }
        String logicalProductId = deterministicId(groupAgency, groupId, "#logicalproduct");
        Ddi4LogicalProduct logicalProduct = new Ddi4LogicalProduct(
            Ddi4LogicalProduct.TYPE, CogsDate.ofDateTime(ColecticaDates.nowIso()),
            urn(groupAgency, logicalProductId), groupAgency, logicalProductId, "1",
            LangStrings.of(defaultLang, "Logical Product"),
            schemeReferencesOfType(newSchemeRefs, CODE_LIST_SCHEME),
            schemeReferencesOfType(newSchemeRefs, CATEGORY_SCHEME),
            null,
            schemeReferencesOfType(newSchemeRefs, MANAGED_REPRESENTATION_SCHEME));
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toLogicalProductItem(logicalProduct)));
        logger.info("Auto-provisioned logical product {}/{} for group {}/{} filing {} scheme(s)",
            groupAgency, logicalProductId, groupAgency, groupId, newSchemeRefs.size());
        reRegisterGroupWithLogicalProducts(groupAgency, groupId,
            List.of(Reference.of(groupAgency, logicalProductId, "1", LOGICAL_PRODUCT)), colecticaItems);
    }

    /**
     * Range les variables de la PhysicalInstance sous le VariableScheme de sa StudyUnit. Si la
     * StudyUnit expose déjà un VariableScheme (via {@code StudyUnit → LogicalProduct →
     * VariableScheme}), les nouvelles références y sont fusionnées ; sinon un VariableScheme et un
     * LogicalProduct neufs sont créés et la StudyUnit est réenregistrée en pointant dessus.
     *
     * <p>Le réenregistrement est sauté (et tracé) quand la StudyUnit est déjà réenregistrée dans le
     * même batch — le flux de duplication qui lui rattache la PhysicalInstance, par exemple — pour
     * éviter d'émettre deux items StudyUnit contradictoires.
     */
    private void appendStudyUnitVariableSchemeUpdate(
        PhysicalInstanceParents parents,
        List<Ddi4Variable> variables,
        List<ColecticaItemResponse> colecticaItems,
        List<ColecticaItemResponse> additionalItems
    ) {
        String suAgency = parents.studyUnitAgency();
        String suId = parents.studyUnitId();
        List<Reference> newRefs = variables.stream()
            .map(v -> Reference.of(v.agency(), v.id(), v.version(), Ddi4Variable.TYPE))
            .toList();

        Optional<ItemReference> schemeRefOpt = findContainerScheme(suAgency, suId, VARIABLE_SCHEME);
        if (schemeRefOpt.isPresent()) {
            ItemReference schemeRef = schemeRefOpt.get();
            Ddi4VariableScheme current = ddi3ToDdi4Converter.toVariableScheme(itemXml(schemeRef));
            List<Reference> merged = mergeReferences(current.variableReference(), newRefs);
            if (merged == null) {
                return;
            }
            Ddi4VariableScheme updated = new Ddi4VariableScheme(current.type(), current.versionDate(),
                current.urn(), current.agency(), current.id(), current.version(), current.label(), merged);
            colecticaItems.add(ColecticaItems.toColecticaItem(
                ddi4ToDdi3Converter.toVariableSchemeItem(updated)));
            logger.info("Filed {} variable(s) under variable scheme {}/{} of study unit {}/{}",
                newRefs.size(), schemeRef.agencyId(), schemeRef.identifier(), suAgency, suId);
            return;
        }

        if (isStudyUnitAlreadyInBatch(additionalItems, suAgency, suId)) {
            logger.warn("Skipping variable scheme auto-provision for study unit {}/{}: it is already "
                + "re-registered in the same batch; its variables will not be filed this time", suAgency, suId);
            return;
        }

        String schemeId = deterministicId(suAgency, suId, "#variablescheme");
        Ddi4VariableScheme scheme = new Ddi4VariableScheme(Ddi4VariableScheme.TYPE,
            CogsDate.ofDateTime(ColecticaDates.nowIso()), urn(suAgency, schemeId),
            suAgency, schemeId, "1", LangStrings.of(defaultLang, "Variable Scheme"), newRefs);
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toVariableSchemeItem(scheme)));

        Reference logicalProductRef = appendSchemeLogicalProduct(
            suAgency, suId, "#studyunit-logicalproduct", schemeId, VARIABLE_SCHEME, colecticaItems);

        Ddi4StudyUnit studyUnit = ddi3ToDdi4Converter.toStudyUnit(
            colecticaClient.getItem(suAgency, suId, null).item());
        List<Reference> logicalProductRefs = new ArrayList<>(
            orEmpty(studyUnit.logicalProductReferences()));
        logicalProductRefs.add(logicalProductRef);
        Ddi4StudyUnit updatedStudyUnit = new Ddi4StudyUnit(studyUnit.type(), studyUnit.versionDate(),
            studyUnit.urn(), studyUnit.agency(), studyUnit.id(), studyUnit.version(), studyUnit.citation(),
            studyUnit.operationIri(), studyUnit.physicalInstanceReferences(), logicalProductRefs);
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toStudyUnitItem(updatedStudyUnit, STUDY_UNIT_UUID)));

        logger.info("Auto-provisioned variable scheme {}/{} for study unit {}/{} and filed {} variable(s)",
            suAgency, schemeId, suAgency, suId, newRefs.size());
    }

    /**
     * Ajoute aux références déjà portées par le logical product celles de {@code schemeType} présentes
     * dans {@code newSchemeRefs}, en renvoyant {@code null} quand le résultat est vide (l'élément
     * {@code <…SchemeReference>} n'est alors simplement pas écrit).
     */
    private static List<Reference> addSchemeReferences(
        List<Reference> existing, List<Reference> newSchemeRefs, String schemeType
    ) {
        List<Reference> refs = new ArrayList<>(orEmpty(existing));
        refs.addAll(newSchemeRefs.stream().filter(ref -> schemeType.equals(ref.type())).toList());
        return refs.isEmpty() ? null : refs;
    }

    private static List<Reference> schemeReferencesOfType(List<Reference> schemeRefs, String schemeType) {
        return addSchemeReferences(null, schemeRefs, schemeType);
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

    /**
     * Cherche le scheme d'un conteneur (Group ou StudyUnit) en parcourant
     * {@code conteneur → LogicalProduct → scheme} via les relations {@code bysubject}. Vide quand il
     * n'en existe pas encore (l'appelant en auto-provisionne alors un).
     */
    private Optional<ItemReference> findContainerScheme(
        String containerAgency, String containerId, String schemeTypeKey
    ) {
        return findScheme(findContainerLogicalProducts(containerAgency, containerId), schemeTypeKey);
    }

    /** Les LogicalProducts rangés par un conteneur (Group ou StudyUnit), via {@code bysubject}. */
    private List<ItemReference> findContainerLogicalProducts(String containerAgency, String containerId) {
        return ColecticaRelationships.childrenOfType(
            colecticaClient, new ItemReference(containerAgency, containerId), itemType(LOGICAL_PRODUCT));
    }

    /**
     * Cherche un scheme rangé sous l'un des {@code logicalProducts}. Vide quand il n'en existe pas
     * encore (l'appelant en auto-provisionne alors un).
     */
    private Optional<ItemReference> findScheme(
        List<ItemReference> logicalProducts, String schemeTypeKey
    ) {
        String schemeType = itemType(schemeTypeKey);
        for (ItemReference logicalProduct : logicalProducts) {
            Optional<ItemReference> scheme = colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT, logicalProduct, List.of(schemeType))
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

    /**
     * Crée et ajoute un LogicalProduct (identifiant déterministe à partir du conteneur et de
     * {@code lpSeedSuffix}) rangeant le scheme {@code schemeId} de type {@code schemeTypeKey}, et
     * renvoie une référence vers lui (à ajouter au conteneur par son réenregistrement).
     */
    private Reference appendSchemeLogicalProduct(
        String containerAgency, String containerId, String lpSeedSuffix,
        String schemeId, String schemeTypeKey, List<ColecticaItemResponse> colecticaItems
    ) {
        String logicalProductId = deterministicId(containerAgency, containerId, lpSeedSuffix);
        Reference schemeReference = Reference.of(containerAgency, schemeId, "1", schemeTypeKey);
        Ddi4LogicalProduct logicalProduct = new Ddi4LogicalProduct(
            Ddi4LogicalProduct.TYPE, CogsDate.ofDateTime(ColecticaDates.nowIso()),
            urn(containerAgency, logicalProductId), containerAgency, logicalProductId, "1",
            LangStrings.of(defaultLang, "Logical Product"),
            CODE_LIST_SCHEME.equals(schemeTypeKey) ? List.of(schemeReference) : null,
            CATEGORY_SCHEME.equals(schemeTypeKey) ? List.of(schemeReference) : null,
            VARIABLE_SCHEME.equals(schemeTypeKey) ? List.of(schemeReference) : null);
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toLogicalProductItem(logicalProduct)));
        return Reference.of(containerAgency, logicalProductId, "1", LOGICAL_PRODUCT);
    }

    /**
     * Réenregistre le groupe (RegisterOrReplace, même version) avec {@code newLogicalProductRefs}
     * ajoutées à ses LogicalProductReferences existantes, établissant la chaîne
     * {@code Group → LogicalProduct → scheme} que parcourent les lectures.
     */
    private void reRegisterGroupWithLogicalProducts(
        String groupAgency, String groupId,
        List<Reference> newLogicalProductRefs, List<ColecticaItemResponse> colecticaItems
    ) {
        Ddi4Group group = ddi3ToDdi4Converter.toGroup(
            colecticaClient.getItem(groupAgency, groupId, null).item());
        List<Reference> logicalProductRefs = new ArrayList<>(orEmpty(group.logicalProductReference()));
        logicalProductRefs.addAll(newLogicalProductRefs);
        Ddi4Group updatedGroup = new Ddi4Group(
            group.type(), group.versionDate(), group.urn(), group.agency(), group.id(), group.version(),
            group.versionResponsibility(), group.citation(), group.studyUnitReference(),
            group.seriesIris(), group.typeOfGroup(), logicalProductRefs);
        colecticaItems.add(ColecticaItems.toColecticaItem(
            ddi4ToDdi3Converter.toGroupItem(updatedGroup, ColecticaItemTypes.GROUP_UUID)));
    }

    private static boolean isStudyUnitAlreadyInBatch(
        List<ColecticaItemResponse> additionalItems, String suAgency, String suId
    ) {
        return additionalItems.stream().anyMatch(item ->
            STUDY_UNIT_UUID.equals(item.itemType())
                && suAgency.equals(item.agencyId())
                && suId.equals(item.identifier()));
    }

    private String itemXml(ItemReference ref) {
        return colecticaClient.getItem(ref.agencyId(), ref.identifier(), null).item();
    }

    private static String deterministicId(String agency, String id, String seedSuffix) {
        return AbstractColecticaItemRepository.generateDeterministicUuid(agency + "/" + id + seedSuffix);
    }

    private static String urn(String agency, String id) {
        return "urn:ddi:%s:%s:1".formatted(agency, id);
    }

    private String itemType(String typeKey) {
        return instanceConfiguration.itemTypes().get(typeKey);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list != null ? list : List.of();
    }
}
