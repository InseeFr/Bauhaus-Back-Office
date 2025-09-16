package fr.insee.rmes.infrastructure.webservice.datasets;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.config.auth.security.UserDecoder;
<<<<<<< HEAD
import fr.insee.rmes.onion.domain.exceptions.RmesException;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/infrastructure/webservice/datasets/DistributionResourcesTest.java
=======
=======
import fr.insee.rmes.domain.exceptions.RmesException;
>>>>>>> 895fe5ae (refactor: migrate getFamily et getFamilies to the hexagonale architecture (#995))
import fr.insee.rmes.onion.infrastructure.webservice.datasets.DistributionResources;
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/infrastructure/webservice/datasets/DistributionResourcesTest.java
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class DistributionResourcesTest {

    @MockitoBean
    DistributionService distributionService;

    @MockitoBean
    DatasetService datasetService;

    @MockitoBean
    UserDecoder userDecoder;

    @Test
    void shouldReturnResponseWhenDeleteDistribution() throws RmesException {
        doNothing().when(distributionService).deleteDistributionId("distribution id mocked");
        DistributionResources distributionResources = new DistributionResources(distributionService,datasetService,userDecoder);
        String actual = distributionResources.deleteDistribution("distribution id mocked").toString();
        Assertions.assertEquals("<200 OK OK,[]>",actual);
    }
    
}