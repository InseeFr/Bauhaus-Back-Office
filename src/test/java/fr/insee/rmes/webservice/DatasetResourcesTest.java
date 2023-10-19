package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.dataset.DatasetResources;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class DatasetResourcesTest {
    @InjectMocks
    private DatasetResources datasetResources;

    @Mock
    DatasetService datasetService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenFetchingDatasets() throws RmesException {
        when(datasetService.getDatasets()).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = datasetResources.getDatasets();
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasets() throws RmesException {
        when(datasetService.getDatasets()).thenReturn("result");
        ResponseEntity<?> response = datasetResources.getDatasets();
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenFetchingDataset() throws RmesException {
        when(datasetService.getDatasetByID(anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = datasetResources.getDataset("");
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDataset() throws RmesException {
        when(datasetService.getDatasetByID(anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.getDataset("1");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenFetchingDistributions() throws RmesException {
        when(datasetService.getDistributions(anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = datasetResources.getDistributionsByDataset("");
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDistributions() throws RmesException {
        when(datasetService.getDistributions(anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.getDistributionsByDataset("1");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenPostingADataset() throws RmesException {
        when(datasetService.create(anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = datasetResources.setDataset("");
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusCode().value());
    }

    @Test
    void shouldReturn201IfRmesExceptionWhenPostingADataset() throws RmesException {
        when(datasetService.create(anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.setDataset("");
        Assertions.assertEquals(HttpStatus.SC_CREATED, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenUpdatingADataset() throws RmesException {
        when(datasetService.update(anyString(), anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = datasetResources.setDataset("", "");
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenUpdatingADataset() throws RmesException {
        when(datasetService.update(anyString(), anyString())).thenReturn("result");
        ResponseEntity<?> response = datasetResources.setDataset("", "");
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }
}
