package fr.insee.rmes.testcontainers.queries.bauhaus_services.distribution;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.commons.domain.ValidationStatus;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@AppSpringBootTest
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
        assertEquals("value lg1", distribution.getLabelLg1());
        assertEquals("value lg2", distribution.getLabelLg2());
        assertEquals(ValidationStatus.UNPUBLISHED.toString(), distribution.getValidationState());
        assertNotNull(distribution.getCreated());
        assertNotNull(distribution.getUpdated());
    }

    @Test
    void should_create_get_update_distribution() throws Exception {
        String id = distributionService.create("""
                {
                    "labelLg1": "value lg1",
                    "labelLg2": "value lg2",
                    "idDataset": "1"
                }
                """);
        assertNotNull(id);

        Distribution distribution = distributionService.getDistributionByID(id);
        assertDistribution(distribution);

        distributionService.update(id, """
                {
                    "labelLg1": "value lg1 updated",
                    "labelLg2": "value lg2 updated",
                    "idDataset": "1"
                }
                """);

        Distribution distribution2 = distributionService.getDistributionByID(id);

        assertEquals("value lg1 updated", distribution2.getLabelLg1());
        assertEquals("value lg2 updated", distribution2.getLabelLg2());

        assertThrows(RmesBadRequestException.class, () -> distributionService.deleteDistributionId(id));
    }

    @Test
    void should_create_get_delete_distribution() throws Exception {
        String id = distributionService.create("""
                {
                    "labelLg1": "value lg1",
                    "labelLg2": "value lg2",
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
