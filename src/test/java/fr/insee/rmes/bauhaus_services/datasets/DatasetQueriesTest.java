package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
public class DatasetQueriesTest {

    @Autowired
    Config config;

    @Test
    void shouldCallGetDatasetsQuery() throws RmesException {
        DatasetQueries.setConfig(config);

        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("LG1", "fr");
                put("DATASET_GRAPH", "datasets-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasets.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDatasets("datasets-graph");
            Assertions.assertEquals(query, "request");
        }
    }

    @Test
    void shouldCallGetDatasetQuery() throws RmesException {
        DatasetQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("LG1", "fr");
                put("LG2", "en");
                put("DATASET_GRAPH", "datasets-graph");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDataset.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDataset("1", "datasets-graph");
            Assertions.assertEquals(query, "request");
        }
    }

    @Test
    void shouldCallGetLastDatasetIdQuery() throws RmesException {
        DatasetQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "datasets-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getLastDatasetId.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.lastDatasetId("datasets-graph");
            Assertions.assertEquals(query, "request");
        }
    }
}
