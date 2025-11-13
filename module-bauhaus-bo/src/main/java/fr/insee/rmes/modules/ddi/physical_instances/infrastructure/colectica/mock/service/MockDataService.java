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

@Service
@ConditionalOnProperty(name = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled", havingValue = "true")
public class MockDataService {
    private static final Logger logger = LoggerFactory.getLogger(MockDataService.class);

    private final DDIRepository secondaryDDIRepository;

    public MockDataService(
            @Qualifier("secondaryDDIRepository") DDIRepository secondaryDDIRepository) {
        this.secondaryDDIRepository = secondaryDDIRepository;
        logger.info("Mock Colectica service initialized using secondary DDI repository");
    }

    /**
     * Get all physical instances from the secondary Colectica instance
     * @return list of partial physical instances
     */
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Mock service: Getting physical instances from secondary repository");
        return secondaryDDIRepository.getPhysicalInstances();
    }

    /**
     * Get a specific physical instance by ID from the secondary Colectica instance
     * @param id the physical instance identifier
     * @return DDI4 response containing the physical instance details
     */
    public Ddi4Response getPhysicalInstanceById(String id) {
        logger.info("Mock service: Getting physical instance with id {} from secondary repository", id);
        return secondaryDDIRepository.getPhysicalInstance(id);
    }
}