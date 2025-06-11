package fr.insee.rmes.webservice.datasets;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
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