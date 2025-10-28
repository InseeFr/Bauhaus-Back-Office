package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetQueries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

class DatasetQueriesTest {

    @BeforeAll
    static void init(){
        DatasetQueries.setConfig(new ConfigStub());
    }

    @Test
    void shouldCallGetDatasetsQueryWithoutStamp() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("LG1", "fr");
                put("DATASET_GRAPH", "datasets-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasets.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDatasets("datasets-graph", null);
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetsQueryWithStamp() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("LG1", "fr");
                put("DATASET_GRAPH", "datasets-graph");
                put("STAMP", "stamp");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasets.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDatasets("datasets-graph", "stamp");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("LG1", "fr");
                put("LG2", "en");
                put("DATASET_GRAPH", "datasets-graph");
                put("ADMS_GRAPH", "adms-graph");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDataset.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDataset("1", "datasets-graph", "adms-graph");
            Assertions.assertEquals("request", query);
        }
    }


    @Test
    void shouldCallGetDatasetCreatorsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "datasets-graph");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasetCreators.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDatasetCreators("1", "datasets-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetSpacialResolutionsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "datasets-graph");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasetSpacialResolutions.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDatasetSpacialResolutions("1", "datasets-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetStatisticalUnitsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "datasets-graph");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasetStatisticalUnits.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getDatasetStatisticalUnits("1", "datasets-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetLastDatasetIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "datasets-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getLastDatasetId.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.lastDatasetId("datasets-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetContributorsByDatasetUri() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH_URI", "datasets-graph-uri");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasetsContributorsByUriQuery.ftlh"), eq(map))).thenReturn("request");
            String query = DatasetQueries.getContributorsByDatasetUri("datasets-graph-uri");
            Assertions.assertEquals("request",query);
        }
    }
}
