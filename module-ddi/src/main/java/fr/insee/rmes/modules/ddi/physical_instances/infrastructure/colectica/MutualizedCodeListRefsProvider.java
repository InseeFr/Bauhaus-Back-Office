package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration.PackageRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Single source of truth for "what is mutualized": every CodeList reference (agency/identifier)
 * reachable from the configured mutualized codes package, obtained by walking its tree top-down
 * ({@code package → CodeListScheme → CodeListGroup → CodeList}).
 *
 * <p>The walk is expensive (several relationship queries against Colectica), so its result is cached
 * through the Spring Cache abstraction ({@link ColecticaCacheNames#MUTUALIZED_PACKAGE_CODE_LIST_REFS},
 * TTL configured via {@code mutualized-codes-cache-ttl}). This logic lives in its own bean rather than
 * inside {@code DDIRepositoryImpl} because both the read path ({@code getMutualizedCodesLists}) and the
 * write path ({@code filterNonMutualizedCodeLists}) call it: as a separate proxied bean the
 * {@code @Cacheable} interception applies to every call, whereas an internal (self-invocation) call
 * would bypass the cache.
 */
public class MutualizedCodeListRefsProvider {

    private static final Logger logger = LoggerFactory.getLogger(MutualizedCodeListRefsProvider.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaConfiguration colecticaConfiguration;
    private final ColecticaClient colecticaClient;

    public MutualizedCodeListRefsProvider(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaConfiguration colecticaConfiguration,
        ColecticaClient colecticaClient
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaConfiguration = colecticaConfiguration;
        this.colecticaClient = colecticaClient;
    }

    /**
     * Returns every CodeList reference reachable from the configured mutualized codes package,
     * deduplicated and in walk order. Empty when no package is configured. Cached for the configured
     * TTL; concurrent callers see a single recompute.
     */
    @Cacheable(ColecticaCacheNames.MUTUALIZED_PACKAGE_CODE_LIST_REFS)
    public List<ItemReference> codeListRefs() {
        PackageRef rootPackage = colecticaConfiguration.mutualizedCodesPackage();
        if (rootPackage == null) {
            return List.of();
        }
        String packageKey = rootPackage.agencyId() + "/" + rootPackage.identifier();
        Map<String, String> itemTypes = instanceConfiguration.itemTypes();
        String codeListSchemeType = itemTypes.get("CodeListScheme");
        String codeListGroupType = itemTypes.get("CodeListGroup");
        String codeListType = itemTypes.get("CodeList");

        long t0 = System.currentTimeMillis();
        Set<ItemReference> codeListRefs = new LinkedHashSet<>();
        for (ItemReference scheme : childrenOfType(rootPackage.agencyId(), rootPackage.identifier(), codeListSchemeType)) {
            for (ItemReference group : childrenOfType(scheme.agencyId(), scheme.identifier(), codeListGroupType)) {
                codeListRefs.addAll(childrenOfType(group.agencyId(), group.identifier(), codeListType));
            }
        }
        logger.info("Walked package {} tree → {} CodeList reference(s) in {} ms",
            packageKey, codeListRefs.size(), System.currentTimeMillis() - t0);
        return List.copyOf(codeListRefs);
    }

    /**
     * Returns the children of {@code agencyId/identifier} of the given item type, via a server-side
     * type-filtered {@code bysubject} relationship query. Returns an empty list (logging a warning)
     * when the lookup fails, so one broken branch does not abort the whole tree walk.
     */
    private List<ItemReference> childrenOfType(String agencyId, String identifier, String childType) {
        try {
            return colecticaClient.findRelatedDescriptions(
                RelationshipDirection.BY_SUBJECT,
                new ItemReference(agencyId, identifier),
                List.of(childType));
        } catch (RuntimeException e) {
            logger.warn("bysubject lookup failed for {}/{}: {}", agencyId, identifier, e.getMessage());
            return List.of();
        }
    }
}
