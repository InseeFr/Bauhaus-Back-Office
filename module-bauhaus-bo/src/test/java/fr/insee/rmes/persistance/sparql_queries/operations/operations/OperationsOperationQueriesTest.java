package fr.insee.rmes.persistance.sparql_queries.operations.operations;

import fr.insee.rmes.Config;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class OperationsOperationQueriesTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = new ConfigStub();
        OperationsOperationQueries.setConfig(config);
    }

    @Test
    void shouldCheckPrefLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel 'Test Operation'@en }");

            String result = OperationsOperationQueries.checkPrefLabelUnicity("op123", "Test Operation", "en");

            assertNotNull(result);
            assertEquals("ASK { ?s skos:prefLabel 'Test Operation'@en }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "op123".equals(map.get("ID")) &&
                               "Test Operation".equals(map.get("LABEL")) &&
                               "en".equals(map.get("LANG")) &&
                               "/operations/operation/".equals(map.get("URI_PREFIX")) &&
                               "insee:StatisticalOperation".equals(map.get("TYPE")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldGetOperationsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperations.ftlh"), any(Map.class)))
                    .thenReturn("SELECT * WHERE { ?operation a insee:StatisticalOperation }");

            String result = OperationsOperationQueries.operationsQuery();

            assertNotNull(result);
            assertEquals("SELECT * WHERE { ?operation a insee:StatisticalOperation }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperations.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldGetOperationQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperation.ftlh"), any(Map.class)))
                    .thenReturn("SELECT * WHERE { ?operation dcterms:identifier 'op123' }");

            String result = OperationsOperationQueries.operationQuery("op123");

            assertNotNull(result);
            assertEquals("SELECT * WHERE { ?operation dcterms:identifier 'op123' }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperation.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "op123".equals(map.get("ID")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldGetSeriesQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeries.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?series WHERE { ?operation insee:isPartOf ?series }");

            String result = OperationsOperationQueries.seriesQuery("op123");

            assertNotNull(result);
            assertEquals("SELECT ?series WHERE { ?operation insee:isPartOf ?series }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeries.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "op123".equals(map.get("ID")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldGetOperationsWithoutSimsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getOperationsWithoutSimsQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?operation WHERE { ?operation insee:isPartOf ?series }");

            String result = OperationsOperationQueries.operationsWithoutSimsQuery("series123");

            assertNotNull(result);
            assertEquals("SELECT ?operation WHERE { ?operation insee:isPartOf ?series }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getOperationsWithoutSimsQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "series123".equals(map.get("ID")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldGetOperationsWithSimsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getOperationsWithSimsQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?operation ?sims WHERE { ?operation insee:isPartOf ?series }");

            String result = OperationsOperationQueries.operationsWithSimsQuery("series456");

            assertNotNull(result);
            assertEquals("SELECT ?operation ?sims WHERE { ?operation insee:isPartOf ?series }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getOperationsWithSimsQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "series456".equals(map.get("ID")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldGetSeriesWithSimsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeriesWithSimsQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?series ?sims WHERE { ?series insee:isPartOf ?family }");

            String result = OperationsOperationQueries.seriesWithSimsQuery("family789");

            assertNotNull(result);
            assertEquals("SELECT ?series ?sims WHERE { ?series insee:isPartOf ?family }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeriesWithSimsQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "family789".equals(map.get("ID_FAMILY")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldHandleNullValuesInCheckPrefLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel ?label }");

            String result = OperationsOperationQueries.checkPrefLabelUnicity(null, null, null);

            assertNotNull(result);
            assertEquals("ASK { ?s skos:prefLabel ?label }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return map.get("ID") == null &&
                               map.get("LABEL") == null &&
                               map.get("LANG") == null;
                    })));
        }
    }

    @Test
    void shouldHandleEmptyStringsInOperationQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperation.ftlh"), any(Map.class)))
                    .thenReturn("SELECT * WHERE { ?operation dcterms:identifier '' }");

            String result = OperationsOperationQueries.operationQuery("");

            assertNotNull(result);
            assertEquals("SELECT * WHERE { ?operation dcterms:identifier '' }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperation.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "".equals(map.get("ID"));
                    })));
        }
    }

    @Test
    void shouldVerifyInitParamsContainsAllRequiredParameters() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperations.ftlh"), any(Map.class)))
                    .thenReturn("SELECT * WHERE { ?operation a insee:StatisticalOperation }");

            OperationsOperationQueries.operationsQuery();

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperations.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        // Verify all required parameters from initParams are present
                        return map.containsKey("OPERATIONS_GRAPH") &&
                               map.containsKey("LG1") &&
                               map.containsKey("LG2") &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldUseDifferentTemplatePathsCorrectly() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            // Test operations/ path
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), any(String.class), any(Map.class)))
                    .thenReturn("OPERATIONS_RESULT");
            
            // Test operations/series/ path
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), any(String.class), any(Map.class)))
                    .thenReturn("SERIES_RESULT");

            String operationsResult = OperationsOperationQueries.operationsQuery();
            String seriesResult = OperationsOperationQueries.seriesQuery("test");

            assertEquals("OPERATIONS_RESULT", operationsResult);
            assertEquals("SERIES_RESULT", seriesResult);

            // Verify correct paths are used
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperations.ftlh"), any(Map.class)));
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeries.ftlh"), any(Map.class)));
        }
    }

    @Test
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            RmesException testException = new RmesException(500, "Test error", "Test error message");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperation.ftlh"), any(Map.class)))
                    .thenThrow(testException);

            RmesException exception = assertThrows(RmesException.class, () -> 
                OperationsOperationQueries.operationQuery("test")
            );

            assertEquals(testException, exception);
        }
    }

    @Test
    void shouldVerifyBuildIndicatorRequestMethod() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeriesWithSimsQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?series WHERE { ?series insee:isPartOf ?family }");

            String result = OperationsOperationQueries.seriesWithSimsQuery("family123");

            assertNotNull(result);
            assertEquals("SELECT ?series WHERE { ?series insee:isPartOf ?family }", result);
            
            // Verify that buildIndicatorRequest uses the correct path (operations/series/)
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeriesWithSimsQuery.ftlh"), any(Map.class)));
        }
    }
}