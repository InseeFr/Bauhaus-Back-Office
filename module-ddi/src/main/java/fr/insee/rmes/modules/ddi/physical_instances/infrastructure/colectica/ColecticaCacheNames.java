package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

/**
 * Names of the Spring caches used by the Colectica integration.
 *
 * <p>Shared between the {@code @Cacheable} annotations (module-ddi) and the {@code CacheManager}
 * declaration (module-bauhaus-bo) so both refer to the exact same cache regions. The TTL applied
 * to these caches is configured via {@code fr.insee.rmes.bauhaus.colectica.mutualized-codes-cache-ttl}
 * (see {@link ColecticaConfiguration#mutualizedCacheTtl()}).
 */
public final class ColecticaCacheNames {

    private ColecticaCacheNames() {}

    /** Cache of the mutualized code lists exposed by {@code GET /ddi/mutualized-codes-list}. */
    public static final String MUTUALIZED_CODES_LISTS = "mutualizedCodesLists";

    /** Cache of the CodeList references reachable from the mutualized codes package (read + write paths). */
    public static final String MUTUALIZED_PACKAGE_CODE_LIST_REFS = "mutualizedPackageCodeListRefs";

    /**
     * Cache des lignes de recherche avancée d'instances physiques exposées par
     * {@code GET /ddi/physical-instance/search} (jointure PhysicalInstance / StudyUnit / Group).
     * Invalidé à chaque écriture de PhysicalInstance.
     */
    public static final String PHYSICAL_INSTANCE_SEARCH_ROWS = "physicalInstanceSearchRows";

    /**
     * Contenu (codes + catégories) des listes de codes mutualisées, clé {@code agence/id/version} :
     * une nouvelle version change la clé, l'entrée n'a donc pas à être invalidée. Une liste de 45 000
     * codes coûte ~30 s à relire dans Colectica.
     */
    public static final String MUTUALIZED_CODE_LIST_CONTENTS = "mutualizedCodeListContents";
}
