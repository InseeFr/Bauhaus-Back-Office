package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
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
 * le nombre d'aller-retours Colectica (de {@code 1 + N + M} à {@code G} appels, G = nombre de groupes).
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
        for (ItemReference group : groupRefs) {
            codeListRefs.addAll(childrenOfType(group.agencyId(), group.identifier(), codeListType));
        }
        logger.info(
                "Resolved {} configured CodeListGroup(s) → {} CodeList reference(s) in {} ms",
                groupRefs.size(),
                codeListRefs.size(),
                System.currentTimeMillis() - t0);
        return List.copyOf(codeListRefs);
    }

    /**
     * CodeList enfants de {@code agencyId/identifier} via une requête {@code bysubject} filtrée
     * côté serveur. Tolérante aux pannes : une branche cassée ne fait pas échouer tout le calcul.
     */
    private List<ItemReference> childrenOfType(String agencyId, String identifier, String childType) {
        try {
            return colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT, new ItemReference(agencyId, identifier), List.of(childType));
        } catch (RuntimeException e) {
            logger.warn("bysubject lookup failed for group {}/{}: {}", agencyId, identifier, e.getMessage());
            return List.of();
        }
    }
}
