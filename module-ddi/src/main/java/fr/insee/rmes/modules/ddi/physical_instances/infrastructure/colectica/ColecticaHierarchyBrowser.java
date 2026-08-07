package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST_SCHEME;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.LOGICAL_PRODUCT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_MISSING_VALUES_REPRESENTATION;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_REPRESENTATION_SCHEME;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation descendante dans l'arborescence Colectica (Group → LogicalProduct → scheme → item).
 *
 * <p>Chaque étape interroge les relations {@code bysubject} filtrées côté serveur sur le type
 * attendu ({@code _query/relationship/bysubject/descriptions}) : on ne récupère que des références
 * légères, au lieu du téléchargement complet {@code set/} + {@code _getList} qui ramènerait chaque
 * item (listes de codes, catégories…) juste pour lire son type. Les libellés, absents des
 * descriptions de relation, sont ensuite résolus par les {@code _query} globaux du catalogue.
 */
class ColecticaHierarchyBrowser {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaHierarchyBrowser.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;
    private final ColecticaCatalogRepository catalog;
    private final ColecticaCodeListRepository codeLists;

    ColecticaHierarchyBrowser(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaClient colecticaClient,
        ColecticaCatalogRepository catalog,
        ColecticaCodeListRepository codeLists
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
        this.catalog = catalog;
        this.codeLists = codeLists;
    }

    List<PartialLogicalProduct> getLogicalProductsByGroup(String agencyId, String groupId) {
        logger.info("Fetching logical products for group {}/{}", agencyId, groupId);
        Set<String> logicalProductIds = childIds(agencyId, groupId, LOGICAL_PRODUCT);
        if (logicalProductIds.isEmpty()) {
            return List.of();
        }
        return catalog.getLogicalProducts().stream()
            .filter(lp -> logicalProductIds.contains(lp.id()))
            .toList();
    }

    List<PartialCodeListScheme> getCodeListSchemesByLogicalProduct(String agencyId, String logicalProductId) {
        logger.info("Fetching code list schemes for logical product {}/{}", agencyId, logicalProductId);
        Set<String> codeListSchemeIds = childIds(agencyId, logicalProductId, CODE_LIST_SCHEME);
        if (codeListSchemeIds.isEmpty()) {
            return List.of();
        }
        return catalog.getCodeListSchemes().stream()
            .filter(scheme -> codeListSchemeIds.contains(scheme.id()))
            .toList();
    }

    List<PartialCodesList> getCodeListsByCodeListScheme(String agencyId, String codeListSchemeId) {
        logger.info("Fetching code lists for code list scheme {}/{}", agencyId, codeListSchemeId);
        Set<String> codeListIds = childIds(agencyId, codeListSchemeId, CODE_LIST);
        if (codeListIds.isEmpty()) {
            return List.of();
        }
        return codeLists.resolveCodeListsMetadata(codeListIds);
    }

    /**
     * Les CodeLists servant de valeurs sentinelles dans un groupe (#1566) : celles référencées par les
     * ManagedMissingValuesRepresentations rangées dans les ManagedRepresentationSchemes des
     * LogicalProducts du groupe.
     */
    List<PartialCodesList> getMissingCodesListsByGroup(String agencyId, String groupId) {
        logger.info("Fetching missing (sentinel) code lists for group {}/{}", agencyId, groupId);
        List<ItemReference> refs = descendFromGroup(agencyId, groupId, List.of(
            LOGICAL_PRODUCT,
            MANAGED_REPRESENTATION_SCHEME,
            MANAGED_MISSING_VALUES_REPRESENTATION,
            CODE_LIST));
        if (refs.isEmpty()) {
            return List.of();
        }
        return codeLists.resolveCodeListsMetadata(
            refs.stream().map(ItemReference::identifier).collect(Collectors.toSet()));
    }

    /** Descente {@code bysubject} depuis un groupe, les niveaux étant donnés par leurs clés de type. */
    List<ItemReference> descendFromGroup(String agencyId, String groupId, List<String> typeKeys) {
        return ColecticaRelationships.descend(
            colecticaClient,
            new ItemReference(agencyId, groupId),
            typeKeys.stream().map(this::itemType).toList());
    }

    private Set<String> childIds(String agencyId, String parentId, String childTypeKey) {
        return ColecticaRelationships
            .childrenOfType(colecticaClient, new ItemReference(agencyId, parentId), itemType(childTypeKey))
            .stream()
            .map(ItemReference::identifier)
            .collect(Collectors.toSet());
    }

    private String itemType(String typeKey) {
        return instanceConfiguration.itemTypes().get(typeKey);
    }
}
