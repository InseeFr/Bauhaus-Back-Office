package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.dataset.DatasetsForSearch;
import fr.insee.rmes.model.dataset.PartialDataset;
import fr.insee.rmes.webservice.datasets.DatasetResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

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
        when(datasetService.create(anyString())).thenReturn("result");
        Assertions.assertEquals("result", datasetResources.setDataset(""));
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
