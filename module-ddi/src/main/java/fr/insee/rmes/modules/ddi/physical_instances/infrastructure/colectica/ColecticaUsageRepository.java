package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.DATA_RELATIONSHIP;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.GROUP_UUID;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.STUDY_UNIT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UsageItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Réponses à la question « qui utilise cet objet ? », par remontée du graphe de relations
 * {@code byobject} : CodeList ← Variable ← DataRelationship ← PhysicalInstance ← StudyUnit ← Group.
 */
class ColecticaUsageRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaUsageRepository.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;
    private final ColecticaLabels labels;

    ColecticaUsageRepository(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaClient colecticaClient,
        ColecticaLabels labels
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
        this.labels = labels;
    }

    /** Les variables utilisant une liste de codes, chacune associée à sa PhysicalInstance. */
    List<CodeListVariableUsage> getVariablesUsingCodeList(String codeListAgencyId, String codeListId) {
        logger.info("Fetching variables using code list {}/{}", codeListAgencyId, codeListId);
        return variableUsagesOf(new ItemReference(codeListAgencyId, codeListId), new HashMap<>());
    }

    /**
     * Les variables référençant une ManagedMissingValuesRepresentation (#1566), avec leur
     * PhysicalInstance et leur StudyUnit — même marche que {@link #getVariablesUsingCodeList}.
     */
    List<CodeListVariableUsage> getVariablesUsingMissingValuesRepresentation(String agencyId, String mmvrId) {
        logger.info("Fetching variables using missing values representation {}/{}", agencyId, mmvrId);
        return variableUsagesOf(new ItemReference(agencyId, mmvrId), new HashMap<>());
    }

    /**
     * Les CodeLists dont un code référence la catégorie donnée ({@code byobject}, filtré sur le type
     * CodeList), chacune jointe aux variables qui l'utilisent (même marche que
     * {@link #getVariablesUsingCodeList}) et au Group propriétaire de la StudyUnit — une ligne plate
     * par (CodeList, Variable), que le front regroupe en arbre Group / StudyUnit / Variable / CodeList.
     * Une liste sans variable utilisatrice produit tout de même une ligne (parents nuls).
     *
     * <p>Une catégorie très partagée (« Oui/Non ») traverse beaucoup de listes qui retombent sur les
     * mêmes PhysicalInstance / StudyUnit / Group : les deux mémos sont donc portés par l'appel entier,
     * {@link #variableUsagesOf} compris, et non par liste de codes.
     */
    List<CategoryCodeListUsage> getCodeListsUsingCategory(String categoryAgencyId, String categoryId) {
        logger.info("Fetching code lists using category {}/{}", categoryAgencyId, categoryId);

        List<ColecticaItem> codeLists = colecticaClient.findRelatedItems(
            RelationshipDirection.BY_OBJECT,
            new ItemReference(categoryAgencyId, categoryId),
            List.of(itemType(CODE_LIST)));

        Map<String, ColecticaItem> studyUnitByPiKey = new HashMap<>();
        Map<String, ColecticaItem> groupBySuKey = new HashMap<>();
        List<CategoryCodeListUsage> result = new ArrayList<>();
        for (ColecticaItem codeList : codeLists) {
            UsageItem codeListItem = usageItem(codeList);
            List<CodeListVariableUsage> variableUsages =
                variableUsagesOf(ColecticaItems.itemRef(codeList), studyUnitByPiKey);
            if (variableUsages.isEmpty()) {
                result.add(CategoryCodeListUsage.ofCodeListAlone(codeListItem));
                continue;
            }
            for (CodeListVariableUsage usage : variableUsages) {
                // Le type Group n'est pas déclaré dans la map itemTypes de la configuration :
                // comme getGroups(), on utilise la constante.
                ColecticaItem group = usage.studyUnitId() == null ? null : resolveOnce(
                    groupBySuKey,
                    new ItemReference(usage.studyUnitAgencyId(), usage.studyUnitId()),
                    GROUP_UUID);
                result.add(new CategoryCodeListUsage(
                    usageItem(group),
                    usageItem(usage.studyUnitAgencyId(), usage.studyUnitId(), usage.studyUnitLabel()),
                    usageItem(usage.physicalInstanceAgencyId(), usage.physicalInstanceId(),
                        usage.physicalInstanceLabel()),
                    usageItem(usage.variableAgencyId(), usage.variableId(), usage.variableLabel()),
                    codeListItem));
            }
        }
        return result;
    }

    /**
     * Marche {@code byobject} (« qui référence X ») : start ← Variable ← DataRelationship ←
     * PhysicalInstance ← StudyUnit.
     *
     * @param studyUnitByPiKey mémo PhysicalInstance → StudyUnit. Toutes les variables d'un même
     *                         fichier partagent sa StudyUnit : sans mémo, une liste utilisée par
     *                         cinquante variables la redemanderait cinquante fois. Le mémo est un
     *                         paramètre pour qu'un appelant qui enchaîne plusieurs marches
     *                         ({@link #getCodeListsUsingCategory}) le partage entre elles.
     */
    private List<CodeListVariableUsage> variableUsagesOf(
        ItemReference start, Map<String, ColecticaItem> studyUnitByPiKey
    ) {
        // L'endpoint /descriptions renvoie déjà l'ItemName/Label de chaque item lié : on utilise
        // findRelatedItems (qui les conserve) pour les niveaux dont on veut le libellé — sans requête
        // de libellés séparée ni appel HTTP supplémentaire. Les DataRelationships ne sont
        // qu'intermédiaires, des références nues suffisent.
        List<ColecticaItem> variables = colecticaClient.findRelatedItems(
            RelationshipDirection.BY_OBJECT, start, List.of(itemType(VARIABLE)));
        if (variables.isEmpty()) {
            return List.of();
        }

        String dataRelationshipType = itemType(DATA_RELATIONSHIP);
        String physicalInstanceType = itemType(PHYSICAL_INSTANCE);
        String studyUnitType = itemType(STUDY_UNIT);

        List<CodeListVariableUsage> usages = new ArrayList<>();
        for (ColecticaItem variable : variables) {
            List<ItemReference> dataRelationships = colecticaClient.findRelatedDescriptions(
                RelationshipDirection.BY_OBJECT, ColecticaItems.itemRef(variable),
                List.of(dataRelationshipType));
            for (ItemReference dataRelationship : dataRelationships) {
                List<ColecticaItem> physicalInstances = colecticaClient.findRelatedItems(
                    RelationshipDirection.BY_OBJECT, dataRelationship, List.of(physicalInstanceType));
                for (ColecticaItem physicalInstance : physicalInstances) {
                    ColecticaItem studyUnit = resolveOnce(
                        studyUnitByPiKey, ColecticaItems.itemRef(physicalInstance), studyUnitType);
                    usages.add(new CodeListVariableUsage(
                        studyUnit == null ? null : studyUnit.agencyId(),
                        studyUnit == null ? null : studyUnit.identifier(),
                        studyUnit == null ? null : labels.of(studyUnit),
                        physicalInstance.agencyId(),
                        physicalInstance.identifier(),
                        labels.of(physicalInstance),
                        variable.agencyId(),
                        variable.identifier(),
                        labels.of(variable)));
                }
            }
        }
        return usages.stream().distinct().toList();
    }

    /**
     * Premier item de {@code itemType} référençant {@code from}, mémoïsé pour la durée de l'appel.
     * L'absence de résultat est mémoïsée elle aussi : un item sans parent résolvable n'est interrogé
     * qu'une fois, là où {@code computeIfAbsent} le rejouerait à chaque passage.
     */
    private ColecticaItem resolveOnce(
        Map<String, ColecticaItem> memo, ItemReference from, String itemType
    ) {
        String key = ColecticaItems.key(from.agencyId(), from.identifier());
        if (memo.containsKey(key)) {
            return memo.get(key);
        }
        ColecticaItem resolved = colecticaClient
            .findRelatedItems(RelationshipDirection.BY_OBJECT, from, List.of(itemType))
            .stream().findFirst().orElse(null);
        memo.put(key, resolved);
        return resolved;
    }

    private UsageItem usageItem(ColecticaItem item) {
        return item == null ? null : new UsageItem(item.agencyId(), item.identifier(), labels.of(item));
    }

    /** Un niveau non résolu (pas de StudyUnit trouvée, par exemple) est absent, pas vide. */
    private static UsageItem usageItem(String agencyId, String id, String label) {
        return id == null ? null : new UsageItem(agencyId, id, label);
    }

    private String itemType(String typeKey) {
        return instanceConfiguration.itemTypes().get(typeKey);
    }
}
