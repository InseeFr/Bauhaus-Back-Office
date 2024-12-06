package fr.insee.rmes.testcontainers.queries.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DistributionServiceImplTest extends WithGraphDBContainer  {
    @Autowired
    DistributionService distributionService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> "bauhaus-test");
    }

    private void assertDistribution(Distribution distribution){
        assertEquals("label lg1", distribution.getLabelLg1());
        assertEquals("label lg2", distribution.getLabelLg2());
        assertEquals(ValidationStatus.UNPUBLISHED.toString(), distribution.getValidationState());
        assertNotNull(distribution.getCreated());
        assertNotNull(distribution.getUpdated());
    }

    @Test
    void should_create_get_update_distribution() throws Exception {
        String id = distributionService.create("""
                {
                    "labelLg1": "label lg1",
                    "labelLg2": "label lg2",
                    "idDataset": "1"
                }
                """);
        assertNotNull(id);

        Distribution distribution = distributionService.getDistributionByID(id);
        assertDistribution(distribution);

        distributionService.update(id, """
                {
                    "labelLg1": "label lg1 updated",
                    "labelLg2": "label lg2 updated",
                    "idDataset": "1"
                }
                """);

        Distribution distribution2 = distributionService.getDistributionByID(id);

        assertEquals("label lg1 updated", distribution2.getLabelLg1());
        assertEquals("label lg2 updated", distribution2.getLabelLg2());

        assertThrows(RmesBadRequestException.class, () -> distributionService.deleteDistributionId(id));
    }

    @Test
    void should_create_get_delete_distribution() throws Exception {
        String id = distributionService.create("""
                {
                    "labelLg1": "label lg1",
                    "labelLg2": "label lg2",
                    "idDataset": "1"
                }
                """);
        assertNotNull(id);

        Distribution distribution = distributionService.getDistributionByID(id);

        assertDistribution(distribution);

        distributionService.deleteDistributionId(id);
        assertThrows(RmesNotFoundException.class, () -> distributionService.getDistributionByID(id));
    }

}
