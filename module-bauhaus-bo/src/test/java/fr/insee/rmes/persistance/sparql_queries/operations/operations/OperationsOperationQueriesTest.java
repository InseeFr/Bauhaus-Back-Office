package fr.insee.rmes.persistance.sparql_queries.operations.operations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
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

    private OperationsOperationQueries operationsOperationQueries;

    @BeforeEach
    void setUp() {
        operationsOperationQueries = new OperationsOperationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
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
                        return "\"Test Operation\"@en".equals(map.get("LABEL")) &&
                               "\"/operations/operation/op123\"".equals(map.get("URI_SUFFIX")) &&
                               "insee:StatisticalOperation".equals(map.get("TYPE")) &&
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH"));
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
                        return ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH")) &&
                               "\"fr\"".equals(map.get("LG1")) &&
                               "\"en\"".equals(map.get("LG2"));
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
                        return "\"/operations/operation/op123\"".equals(map.get("OPERATION_URI_SUFFIX")) &&
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH")) &&
                               "\"fr\"".equals(map.get("LG1")) &&
                               "\"en\"".equals(map.get("LG2"));
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
                        return "\"/operations/operation/op123\"".equals(map.get("OPERATION_URI_SUFFIX")) &&
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH")) &&
                               "\"fr\"".equals(map.get("LG1")) &&
                               "\"en\"".equals(map.get("LG2"));
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
                        return "\"/operations/serie/series123\"".equals(map.get("SERIES_URI_SUFFIX")) &&
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH")) &&
                               "\"fr\"".equals(map.get("LG1")) &&
                               "\"en\"".equals(map.get("LG2"));
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
                        return "\"/operations/serie/series456\"".equals(map.get("SERIES_URI_SUFFIX")) &&
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH")) &&
                               "\"fr\"".equals(map.get("LG1")) &&
                               "\"en\"".equals(map.get("LG2"));
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
                        return "\"/operations/famille/family789\"".equals(map.get("FAMILY_URI_SUFFIX")) &&
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH")) &&
                               "\"fr\"".equals(map.get("LG1")) &&
                               "\"en\"".equals(map.get("LG2"));
                    })));
        }
    }

    @Test
    void shouldRejectNullValuesInCheckPrefLabelUnicity() {
        assertThrows(IllegalArgumentException.class,
                () -> operationsOperationQueries.checkPrefLabelUnicity(null, null, null));
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
                        return "\"/operations/operation/\"".equals(map.get("OPERATION_URI_SUFFIX"));
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
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH")) &&
                               "\"fr\"".equals(map.get("LG1")) &&
                               "\"en\"".equals(map.get("LG2"));
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
