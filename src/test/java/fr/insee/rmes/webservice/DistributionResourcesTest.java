package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.distribution.DistributionResources;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DistributionResourcesTest {
    @InjectMocks
    private DistributionResources distributionResources;

    @Mock
    DistributionService distributionService;

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasets() throws RmesException {
        when(distributionService.getDistributions()).thenReturn("result");
        ResponseEntity<?> response = distributionResources.getDistributions();
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDistributionById() throws RmesException {
        when(distributionService.getDistributionByID(anyString())).thenReturn("result");
        ResponseEntity<?> response = distributionResources.getDistribution("");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn201IfRmesExceptionWhenPostingADistribution() throws RmesException {
        when(distributionService.create(anyString())).thenReturn("result");
        ResponseEntity<?> response = distributionResources.createDistribution("");
        Assertions.assertEquals(HttpStatus.SC_CREATED, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenUpdatingADistribution() throws RmesException {
        when(distributionService.update(anyString(), anyString())).thenReturn("result");
        ResponseEntity<?> response = distributionResources.updateDistribution("", "");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }
}
