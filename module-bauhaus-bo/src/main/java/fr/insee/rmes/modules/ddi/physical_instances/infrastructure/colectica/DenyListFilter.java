package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic filter for applying deny list rules to collections of items.
 *
 * <p>This component provides reusable filtering logic that can be applied to any
 * type of item based on agencyId and id combinations. It uses a cached HashSet
 * for O(1) lookup performance.
 *
 * <p>Example usage:
 * <pre>
 * List&lt;CodeList&gt; filtered = denyListFilter.filterItems(
 *     codeLists,
 *     CodeList::getAgencyId,
 *     CodeList::getId
 * );
 * </pre>
 *
 * @see ColecticaConfiguration
 * @see ColecticaConfiguration.CodeListDenyEntry
 */
@Component
public class DenyListFilter {

    private static final Logger logger = LoggerFactory.getLogger(DenyListFilter.class);

    private final ColecticaConfiguration configuration;
    private Set<String> denyListCache;

    /**
     * Creates a new DenyListFilter with the given configuration.
     *
     * @param configuration The Colectica configuration containing the deny list
     */
    public DenyListFilter(ColecticaConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Filters a list of items based on the configured deny list.
     *
     * <p>Items whose agencyId and id combination matches an entry in the deny list
     * will be excluded from the returned list.
     *
     * <p>If no deny list is configured, the original list is returned unchanged.
     *
     * @param items The items to filter
     * @param agencyIdExtractor Function to extract the agencyId from an item
     * @param idExtractor Function to extract the id from an item
     * @param <T> The type of items being filtered
     * @return A filtered list with denied items removed
     */
    public <T> List<T> filterItems(
            List<T> items,
            Function<T, String> agencyIdExtractor,
            Function<T, String> idExtractor) {

        if (items == null || items.isEmpty()) {
            return items;
        }

        if (configuration == null || configuration.codeListDenyList() == null ||
                configuration.codeListDenyList().isEmpty()) {
            logger.debug("No deny list configured, returning all {} items", items.size());
            return items;
        }

        int originalSize = items.size();

        List<T> filteredItems = items.stream()
                .filter(item -> !isInDenyList(
                        agencyIdExtractor.apply(item),
                        idExtractor.apply(item)))
                .toList();

        int filteredCount = originalSize - filteredItems.size();
        if (filteredCount > 0) {
            logger.info("Filtered {} item(s) from {} total using deny list (returned {} items)",
                    filteredCount, originalSize, filteredItems.size());
        }

        return filteredItems;
    }

    /**
     * Checks if an item with the given agencyId and id is in the deny list.
     *
     * <p>This method uses a cached HashSet for O(1) lookup performance.
     * The cache is lazily initialized on first access.
     *
     * @param agencyId The agency ID to check
     * @param id The item ID to check
     * @return true if the item should be filtered out, false otherwise
     */
    public boolean isInDenyList(String agencyId, String id) {
        if (configuration == null || configuration.codeListDenyList() == null) {
            return false;
        }

        // Lazy initialization of cache for O(1) lookups
        if (denyListCache == null) {
            synchronized (this) {
                if (denyListCache == null) {
                    denyListCache = configuration.codeListDenyList().stream()
                            .map(entry -> createDenyListKey(entry.agencyId(), entry.id()))
                            .collect(Collectors.toSet());
                    logger.info("Initialized deny list cache with {} entries", denyListCache.size());
                }
            }
        }

        String key = createDenyListKey(agencyId, id);
        boolean isDenied = denyListCache.contains(key);

        if (isDenied) {
            logger.debug("Item is in deny list: agencyId={}, id={}", agencyId, id);
        }

        return isDenied;
    }

    /**
     * Creates a unique key for deny list lookups by combining agencyId and id.
     *
     * @param agencyId The agency ID
     * @param id The item ID
     * @return A unique key string in format "agencyId:id"
     */
    private String createDenyListKey(String agencyId, String id) {
        return agencyId + ":" + id;
    }

    /**
     * Returns the current size of the deny list cache.
     *
     * <p>Returns 0 if the cache has not been initialized yet.
     *
     * @return The number of entries in the cache
     */
    public int getCacheSize() {
        return denyListCache != null ? denyListCache.size() : 0;
    }

    /**
     * Returns the current deny list configuration.
     *
     * @return The list of deny list entries, or null if not configured
     */
    public List<ColecticaConfiguration.CodeListDenyEntry> getDenyList() {
        return configuration != null ? configuration.codeListDenyList() : null;
    }
}
