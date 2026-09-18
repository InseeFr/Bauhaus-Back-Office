package fr.insee.rmes.persistance.sparql_queries.operations.operations;

import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.ANY_PARAMS;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertQueryBuiltFromTemplate;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertRmesExceptionPropagated;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.callWithStubbedTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OperationsOperationQueriesTest {

    private static final String OPERATIONS_FOLDER = "operations/";
    private static final String SERIES_FOLDER = "operations/series/";
    private static final String GET_OPERATIONS_QUERY = "SELECT * WHERE { ?operation a insee:StatisticalOperation }";

    private OperationsOperationQueries operationsOperationQueries;

    @BeforeEach
    void setUp() {
        operationsOperationQueries =
                new OperationsOperationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
    }

    @Test
    void shouldCheckPrefLabelUnicity() throws RmesException {
        assertQueryBuiltFromTemplate(
                OPERATIONS_FOLDER,
                "checkFamilyPrefLabelUnicity.ftlh",
                "ASK { ?s skos:prefLabel 'Test Operation'@en }",
                () -> operationsOperationQueries.checkPrefLabelUnicity("op123", "Test Operation", "en"),
                map -> "\"Test Operation\"@en".equals(map.get("LABEL"))
                        && "\"/operations/operation/op123\"".equals(map.get("URI_SUFFIX"))
                        && "insee:StatisticalOperation".equals(map.get("TYPE"))
                        && isOperationsGraph(map));
    }

    @Test
    void shouldGetOperationsQuery() throws RmesException {
        assertQueryBuiltFromTemplate(
                OPERATIONS_FOLDER,
                "getOperations.ftlh",
                GET_OPERATIONS_QUERY,
                operationsOperationQueries::operationsQuery,
                map -> isOperationsGraph(map) && hasLanguages(map));
    }

    @Test
    void shouldGetOperationQuery() throws RmesException {
        assertQueryBuiltFromTemplate(
                OPERATIONS_FOLDER,
                "getOperation.ftlh",
                "SELECT * WHERE { ?operation dcterms:identifier 'op123' }",
                () -> operationsOperationQueries.operationQuery("op123"),
                map -> "\"/operations/operation/op123\"".equals(map.get("OPERATION_URI_SUFFIX"))
                        && isOperationsGraph(map)
                        && hasLanguages(map));
    }

    @Test
    void shouldGetSeriesQuery() throws RmesException {
        assertQueryBuiltFromTemplate(
                SERIES_FOLDER,
                "getSeries.ftlh",
                "SELECT ?series WHERE { ?operation insee:isPartOf ?series }",
                () -> operationsOperationQueries.seriesQuery("op123"),
                map -> "\"/operations/operation/op123\"".equals(map.get("OPERATION_URI_SUFFIX"))
                        && isOperationsGraph(map)
                        && hasLanguages(map));
    }

    @Test
    void shouldGetOperationsWithoutSimsQuery() throws RmesException {
        assertQueryBuiltFromTemplate(
                SERIES_FOLDER,
                "getOperationsWithoutSimsQuery.ftlh",
                "SELECT ?operation WHERE { ?operation insee:isPartOf ?series }",
                () -> operationsOperationQueries.operationsWithoutSimsQuery("series123"),
                map -> "\"/operations/serie/series123\"".equals(map.get("SERIES_URI_SUFFIX"))
                        && isOperationsGraph(map)
                        && hasLanguages(map));
    }

    @Test
    void shouldGetOperationsWithSimsQuery() throws RmesException {
        assertQueryBuiltFromTemplate(
                SERIES_FOLDER,
                "getOperationsWithSimsQuery.ftlh",
                "SELECT ?operation ?sims WHERE { ?operation insee:isPartOf ?series }",
                () -> operationsOperationQueries.operationsWithSimsQuery("series456"),
                map -> "\"/operations/serie/series456\"".equals(map.get("SERIES_URI_SUFFIX"))
                        && isOperationsGraph(map)
                        && hasLanguages(map));
    }

    @Test
    void shouldGetSeriesWithSimsQuery() throws RmesException {
        assertQueryBuiltFromTemplate(
                SERIES_FOLDER,
                "getSeriesWithSimsQuery.ftlh",
                "SELECT ?series ?sims WHERE { ?series insee:isPartOf ?family }",
                () -> operationsOperationQueries.seriesWithSimsQuery("family789"),
                map -> "\"/operations/famille/family789\"".equals(map.get("FAMILY_URI_SUFFIX"))
                        && isOperationsGraph(map)
                        && hasLanguages(map));
    }

    @Test
    void shouldRejectNullValuesInCheckPrefLabelUnicity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> operationsOperationQueries.checkPrefLabelUnicity(null, null, null));
    }

    @Test
    void shouldHandleEmptyStringsInOperationQuery() throws RmesException {
        assertQueryBuiltFromTemplate(
                OPERATIONS_FOLDER,
                "getOperation.ftlh",
                "SELECT * WHERE { ?operation dcterms:identifier '' }",
                () -> operationsOperationQueries.operationQuery(""),
                map -> "\"/operations/operation/\"".equals(map.get("OPERATION_URI_SUFFIX")));
    }

    @Test
    void shouldVerifyInitParamsContainsAllRequiredParameters() throws RmesException {
        callWithStubbedTemplate(
                OPERATIONS_FOLDER,
                "getOperations.ftlh",
                GET_OPERATIONS_QUERY,
                operationsOperationQueries::operationsQuery,
                map -> map.containsKey("OPERATIONS_GRAPH")
                        && map.containsKey("LG1")
                        && map.containsKey("LG2")
                        && isOperationsGraph(map)
                        && hasLanguages(map));
    }

    @Test
    void shouldUseDifferentTemplatePathsCorrectly() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker
                    .when(() -> FreeMarkerUtils.buildRequest(eq(OPERATIONS_FOLDER), any(String.class), any(Map.class)))
                    .thenReturn("OPERATIONS_RESULT");
            mockedFreeMarker
                    .when(() -> FreeMarkerUtils.buildRequest(eq(SERIES_FOLDER), any(String.class), any(Map.class)))
                    .thenReturn("SERIES_RESULT");

            String operationsResult = operationsOperationQueries.operationsQuery();
            String seriesResult = operationsOperationQueries.seriesQuery("test");

            assertEquals("OPERATIONS_RESULT", operationsResult);
            assertEquals("SERIES_RESULT", seriesResult);

            mockedFreeMarker.verify(() ->
                    FreeMarkerUtils.buildRequest(eq(OPERATIONS_FOLDER), eq("getOperations.ftlh"), any(Map.class)));
            mockedFreeMarker.verify(
                    () -> FreeMarkerUtils.buildRequest(eq(SERIES_FOLDER), eq("getSeries.ftlh"), any(Map.class)));
        }
    }

    @Test
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        assertRmesExceptionPropagated(
                OPERATIONS_FOLDER, "getOperation.ftlh", () -> operationsOperationQueries.operationQuery("test"));
    }

    @Test
    void shouldVerifyBuildIndicatorRequestMethod() throws RmesException {
        assertQueryBuiltFromTemplate(
                SERIES_FOLDER,
                "getSeriesWithSimsQuery.ftlh",
                "SELECT ?series WHERE { ?series insee:isPartOf ?family }",
                () -> operationsOperationQueries.seriesWithSimsQuery("family123"),
                ANY_PARAMS);
    }

    private static boolean isOperationsGraph(Map<String, Object> map) {
        return ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH"));
    }

    private static boolean hasLanguages(Map<String, Object> map) {
        return "\"fr\"".equals(map.get("LG1")) && "\"en\"".equals(map.get("LG2"));
    }
}
