package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled", havingValue = "true")
public class MockDataService {
    private static final Logger logger = LoggerFactory.getLogger(MockDataService.class);
    
    private final ObjectMapper objectMapper;
    private final Map<String, ColecticaResponse> cache = new ConcurrentHashMap<>();

    public MockDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    

    public ColecticaResponse getColecticaResponse() {
        String cacheKey = "colectica-response";
        
        return cache.computeIfAbsent(cacheKey, key -> {
            try {
                logger.info("Loading complete Colectica response from JSON file");
                ClassPathResource resource = new ClassPathResource("mock/physical-instances.json");
                
                // Read the complete JSON structure using records
                return objectMapper.readValue(resource.getInputStream(), ColecticaResponse.class);
                    
            } catch (IOException e) {
                logger.error("Error reading Colectica response JSON file", e);
                throw new RuntimeException("Failed to load Colectica response data", e);
            }
        });
    }
    
    public void clearCache() {
        logger.info("Clearing mock data cache");
        cache.clear();
    }
    
    public void clearCache(String key) {
        logger.info("Clearing cache for key: {}", key);
        cache.remove(key);
    }
    

}