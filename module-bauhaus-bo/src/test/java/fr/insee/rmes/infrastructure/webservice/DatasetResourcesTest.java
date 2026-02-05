package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.datasets.datasets.model.Dataset;
import fr.insee.rmes.modules.datasets.datasets.model.DatasetsForSearch;
import fr.insee.rmes.modules.datasets.datasets.model.PartialDataset;
import fr.insee.rmes.modules.datasets.datasets.webservice.DatasetResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetResourcesTest {
    @InjectMocks
    private DatasetResources datasetResources;

    @Mock
    DatasetService datasetService;

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasets() throws RmesException {
        List<PartialDataset> datasets = new ArrayList<>();
        datasets.add(new PartialDataset("1", "label"));

        when(datasetService.getDatasets()).thenReturn(datasets);
        Assertions.assertEquals(1, datasetResources.getDatasets().size());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasetsForSearch() throws RmesException {
        List<DatasetsForSearch> datasets = new ArrayList<>();
        datasets.add(new DatasetsForSearch(
                "id",
                "labelLg1",
                "creator",
                "disseminationStatus",
                "validationStatus",
                "wasGeneratedIRIs",
                "created",
                "updated"
        ));

        when(datasetService.getDatasetsForSearch()).thenReturn(datasets);
        Assertions.assertEquals(1, datasetResources.getDatasetsForSearch().size());
    }


    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDataset() throws RmesException {
        Dataset dataset=new Dataset();
        dataset.setId("1");
        when(datasetService.getDatasetByID(anyString())).thenReturn(dataset);
        Assertions.assertEquals(dataset, datasetResources.getDataset("1"));
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDistributions() throws RmesException {
        when(datasetService.getDistributions(anyString())).thenReturn("result");
        Assertions.assertEquals("result", datasetResources.getDistributionsByDataset("1"));
    }

    @Test
    void shouldReturn201IfRmesExceptionWhenPostingADataset() throws RmesException {
        // Given
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/datasets");
        req.setServerName("localhost");
        req.setServerPort(80);
        req.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        DatasetResources myDatasetResources = new DatasetResources(datasetService);
        String expectedId = "mocked-result";
        when(datasetService.create("mocked body")).thenReturn(expectedId);

        // When
        ResponseEntity<String> response = myDatasetResources.setDataset("mocked body");

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedId, response.getBody());
        assertEquals(
                "/datasets/" + expectedId,
                Objects.requireNonNull(response.getHeaders().getLocation()).getPath()
        );
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenUpdatingADataset() throws RmesException {
        when(datasetService.update(anyString(), anyString())).thenReturn("result");

        Assertions.assertDoesNotThrow(() -> datasetResources.setDataset("", ""));
    }

    @Test
    void shouldCallPublishDataset() throws RmesException {
        when(datasetService.publishDataset("1")).thenReturn("result");
        Assertions.assertDoesNotThrow(() -> datasetResources.publishDataset("1"));
    }
}
