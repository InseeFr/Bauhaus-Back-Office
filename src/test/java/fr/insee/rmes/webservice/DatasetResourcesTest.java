package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.dataset.DatasetResources;
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
public class DatasetResourcesTest {
    @InjectMocks
    private DatasetResources datasetResources;

    @Mock
    DatasetService datasetService;

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasets() throws RmesException {
        when(datasetService.getDatasets()).thenReturn("result");
        ResponseEntity<?> response = datasetResources.getDatasets();
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }


    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDataset() throws RmesException {
        when(datasetService.getDatasetByID(anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.getDataset("1");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDistributions() throws RmesException {
        when(datasetService.getDistributions(anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.getDistributionsByDataset("1");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn201IfRmesExceptionWhenPostingADataset() throws RmesException {
        when(datasetService.create(anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.setDataset("");
        Assertions.assertEquals(HttpStatus.SC_CREATED, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenUpdatingADataset() throws RmesException {
        when(datasetService.update(anyString(), anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.setDataset("", "");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }
}
