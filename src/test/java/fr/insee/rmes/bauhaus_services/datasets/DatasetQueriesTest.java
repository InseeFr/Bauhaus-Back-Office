package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DatasetQueriesTest {

    @Mock
    Config config;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCallGetDatasetsQuery() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getDatasetsGraph()).thenReturn("datasets-graph");
        DatasetQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("LG1", "fr");
                put("DATASET_GRAPH", "datasets-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasets.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDatasets();
            Assertions.assertEquals(query, "request");
        }
    }

    @Test
    void shouldCallGetDatasetQuery() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getDatasetsGraph()).thenReturn("datasets-graph");
        DatasetQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("LG1", "fr");
                put("LG2", "en");
                put("DATASET_GRAPH", "datasets-graph");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDataset.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDataset("1");
            Assertions.assertEquals(query, "request");
        }
    }

    @Test
    void shouldCallGetLastDatasetIdQuery() throws RmesException {
        when(config.getDatasetsGraph()).thenReturn("datasets-graph");
        DatasetQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "datasets-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getLastDatasetId.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.lastDatasetId();
            Assertions.assertEquals(query, "request");
        }
    }
}
