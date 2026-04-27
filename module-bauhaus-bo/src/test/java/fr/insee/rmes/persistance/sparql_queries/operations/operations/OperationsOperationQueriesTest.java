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
    private OperationsOperationQueries operationsOperationQueries;

    @BeforeEach
    void setUp() {
        config = new ConfigStub();
        operationsOperationQueries = new OperationsOperationQueries(config);
    }

    @Test
    void shouldCheckPrefLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel 'Test Operation'@en }");

            String result = operationsOperationQueries.checkPrefLabelUnicity("op123", "Test Operation", "en");

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

            String result = operationsOperationQueries.operationsQuery();

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

            String result = operationsOperationQueries.operationQuery("op123");

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

            String result = operationsOperationQueries.seriesQuery("op123");

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

            String result = operationsOperationQueries.operationsWithoutSimsQuery("series123");

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

            String result = operationsOperationQueries.operationsWithSimsQuery("series456");

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

            String result = operationsOperationQueries.seriesWithSimsQuery("family789");

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
                    .thenReturn("ASK { ?s skos:prefLabel ?value }");

            String result = operationsOperationQueries.checkPrefLabelUnicity(null, null, null);

            assertNotNull(result);
            assertEquals("ASK { ?s skos:prefLabel ?value }", result);
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

            String result = operationsOperationQueries.operationQuery("");

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

            operationsOperationQueries.operationsQuery();

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("getOperations.ftlh"),
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
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
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), any(String.class), any(Map.class)))
                    .thenReturn("OPERATIONS_RESULT");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), any(String.class), any(Map.class)))
                    .thenReturn("SERIES_RESULT");

            String operationsResult = operationsOperationQueries.operationsQuery();
            String seriesResult = operationsOperationQueries.seriesQuery("test");

            assertEquals("OPERATIONS_RESULT", operationsResult);
            assertEquals("SERIES_RESULT", seriesResult);

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
                operationsOperationQueries.operationQuery("test")
            );

            assertEquals(testException, exception);
        }
    }

    @Test
    void shouldVerifyBuildIndicatorRequestMethod() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeriesWithSimsQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?series WHERE { ?series insee:isPartOf ?family }");

            String result = operationsOperationQueries.seriesWithSimsQuery("family123");

            assertNotNull(result);
            assertEquals("SELECT ?series WHERE { ?series insee:isPartOf ?family }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/series/"), eq("getSeriesWithSimsQuery.ftlh"), any(Map.class)));
        }
    }
}
