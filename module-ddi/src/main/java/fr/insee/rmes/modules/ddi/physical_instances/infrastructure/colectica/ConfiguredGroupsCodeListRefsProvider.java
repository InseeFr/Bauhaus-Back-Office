package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

/**
 * Alternative à {@link MutualizedCodeListRefsProvider} : au lieu de parcourir l'arbre complet
 * ({@code package → CodeListScheme → CodeListGroup → CodeList}), on fournit directement en
 * configuration les {@code CodeListGroup} (agency + identifiant) et on ne fait qu'un appel
 * relationship {@code bysubject} par groupe pour récupérer ses {@code CodeList} enfants. Cela réduit
 * le nombre d'aller-retours Colectica (de {@code 1 + N + M} à {@code 1 + G} appels, G = nombre de groupes).
 *
 * <p>Seule la dernière version de chaque groupe compte : elle est résolue en un appel groupé
 * ({@code _getLatestVersionNumbers}), puis seules ses relations sont lues. Une CodeList retirée du
 * groupe par une version ultérieure n'est donc plus considérée comme mutualisée.
 *
 * <p>Le résultat est mis en cache via la même région que la stratégie de walk
 * ({@link ColecticaCacheNames#MUTUALIZED_PACKAGE_CODE_LIST_REFS}) ; une seule stratégie est active
 * à la fois (flag {@code mutualized-codes-strategy}).
 */
public class ConfiguredGroupsCodeListRefsProvider implements MutualizedCodeListRefsStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguredGroupsCodeListRefsProvider.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final List<ItemReference> groupRefs;
    private final ColecticaClient colecticaClient;

    public ConfiguredGroupsCodeListRefsProvider(
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            List<ItemReference> groupRefs,
            ColecticaClient colecticaClient) {
        this.instanceConfiguration = instanceConfiguration;
        this.groupRefs = List.copyOf(groupRefs);
        this.colecticaClient = colecticaClient;
    }

    @Override
    @Cacheable(ColecticaCacheNames.MUTUALIZED_PACKAGE_CODE_LIST_REFS)
    public List<ItemReference> codeListRefs() {
        if (groupRefs.isEmpty()) {
            return List.of();
        }
        String codeListType = instanceConfiguration.itemTypes().get("CodeList");

        long t0 = System.currentTimeMillis();
        Set<ItemReference> codeListRefs = new LinkedHashSet<>();
        for (ColecticaSetItem group : latestGroupVersions()) {
            codeListRefs.addAll(childrenOfType(group.agencyId(), group.identifier(), group.version(), codeListType));
        }
        logger.info(
                "Resolved {} configured CodeListGroup(s) → {} CodeList reference(s) in {} ms",
                groupRefs.size(),
                codeListRefs.size(),
                System.currentTimeMillis() - t0);
        return List.copyOf(codeListRefs);
    }

    /**
     * Dernière version de chaque groupe configuré. Un groupe inconnu de Colectica est absent de la
     * réponse, donc ignoré. Un échec de Colectica, lui, est propagé : le convertir en « aucun groupe »
     * mettrait en cache (TTL {@code mutualized-codes-cache-ttl}) un catalogue vide, et toute liste
     * mutualisée serait alors lue et embarquée comme une liste de groupe. Une exception n'étant pas
     * mise en cache, l'appel suivant réessaie.
     */
    private List<ColecticaSetItem> latestGroupVersions() {
        return colecticaClient.getLatestVersionNumbers(groupRefs);
    }

    /**
     * CodeList enfants de la version {@code version} de {@code agencyId/identifier} via une requête
     * {@code bysubject} filtrée côté serveur. Un échec est propagé, pour la même raison que
     * {@link #latestGroupVersions()} : un groupe manquant donnerait un catalogue partiel mis en cache.
     */
    private List<ItemReference> childrenOfType(String agencyId, String identifier, int version, String childType) {
        return colecticaClient.findRelatedDescriptions(
                RelationshipDirection.BY_SUBJECT, new ItemReference(agencyId, identifier), version, List.of(childType));
    }
}
