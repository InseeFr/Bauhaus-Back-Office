package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock.service;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled", havingValue = "true")
public class MockDataService {
    private static final Logger logger = LoggerFactory.getLogger(MockDataService.class);

    private final DDIRepository secondaryDDIRepository;

    // Cache for all physical instances (list cache)
    private List<PartialPhysicalInstance> physicalInstancesCache;

    // Cache for individual physical instances by ID
    private final Map<String, Ddi4Response> physicalInstanceByIdCache = new ConcurrentHashMap<>();

    public MockDataService(
            @Qualifier("secondaryDDIRepository") DDIRepository secondaryDDIRepository) {
        this.secondaryDDIRepository = secondaryDDIRepository;
        logger.info("Mock Colectica service initialized using secondary DDI repository with caching enabled");
    }

    /**
     * Get all physical instances from the secondary Colectica instance
     * Uses cache to avoid repeated calls to the secondary repository
     * @return list of partial physical instances
     */
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        if (physicalInstancesCache != null) {
            logger.debug("Mock service: Returning {} physical instances from cache", physicalInstancesCache.size());
            return physicalInstancesCache;
        }

        logger.info("Mock service: Getting physical instances from secondary repository (cache miss)");
        physicalInstancesCache = secondaryDDIRepository.getPhysicalInstances();
        logger.info("Mock service: Cached {} physical instances", physicalInstancesCache.size());

        return physicalInstancesCache;
    }

    /**
     * Get a specific physical instance by ID from the secondary Colectica instance
     * Uses cache based on ID to avoid repeated calls to the secondary repository
     * @param id the physical instance identifier
     * @return DDI4 response containing the physical instance details
     */
    public Ddi4Response getPhysicalInstanceById(String id) {
        // Check cache first
        if (physicalInstanceByIdCache.containsKey(id)) {
            logger.debug("Mock service: Returning physical instance with id {} from cache", id);
            return physicalInstanceByIdCache.get(id);
        }

        logger.info("Mock service: Getting physical instance with id {} from secondary repository (cache miss)", id);
        Ddi4Response response = secondaryDDIRepository.getPhysicalInstance(id);

        // Store in cache if not null
        if (response != null) {
            physicalInstanceByIdCache.put(id, response);
            logger.info("Mock service: Cached physical instance with id {}", id);
        }

        return response;
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        logger.info("Mock service: Clearing all caches");
        physicalInstancesCache = null;
        physicalInstanceByIdCache.clear();
    }

    /**
     * Clear the list cache for physical instances
     */
    public void clearPhysicalInstancesCache() {
        logger.info("Mock service: Clearing physical instances list cache");
        physicalInstancesCache = null;
    }

    /**
     * Clear the cache for a specific physical instance by ID
     * @param id the physical instance identifier
     */
    public void clearPhysicalInstanceByIdCache(String id) {
        logger.info("Mock service: Clearing cache for physical instance with id {}", id);
        physicalInstanceByIdCache.remove(id);
    }

    /**
     * Get cache statistics
     * @return map with cache statistics
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
            "physicalInstancesListCached", physicalInstancesCache != null,
            "physicalInstancesListCount", physicalInstancesCache != null ? physicalInstancesCache.size() : 0,
            "physicalInstancesByIdCount", physicalInstanceByIdCache.size()
        );
    }
}