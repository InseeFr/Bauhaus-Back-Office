package fr.insee.rmes.persistance.sparql_queries.operations.famOpeSerUtils;

import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.ANY_PARAMS;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertQueryBuiltFromTemplate;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertRmesExceptionPropagated;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.callWithStubbedTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationQueries;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OperationQueriesTest {

    private static final String FAM_OPE_SER_FOLDER = "operations/famOpeSer/";
    private static final String LAST_ID_TEMPLATE = "getLastIdQuery.ftlh";
    private static final String LAST_ID_QUERY = "SELECT ?lastId WHERE { ?s dcterms:identifier ?lastId }";
    private static final String PUBLICATION_STATUS_TEMPLATE = "getPublicationStatusQuery.ftlh";
    private static final String PUBLICATION_STATUS_QUERY = "SELECT ?state WHERE { ?s insee:validationState ?state }";

    private OperationQueries operationQueries;

    @BeforeEach
    void setUp() {
        operationQueries =
                new OperationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
    }

    @Test
    void shouldGetLastId() throws RmesException {
        assertQueryBuiltFromTemplate(
                FAM_OPE_SER_FOLDER,
                LAST_ID_TEMPLATE,
                LAST_ID_QUERY,
                operationQueries::lastId,
                map -> "\"fr\"".equals(map.get("LG1")) && "\"en\"".equals(map.get("LG2")) && isOperationsGraph(map));
    }

    @Test
    void shouldCheckIfFamOpeSerExists() throws RmesException {
        String testUri = "http://example.org/operation/123";
        assertQueryBuiltFromTemplate(
                FAM_OPE_SER_FOLDER,
                "checkIfFamSerOpeExistsQuery.ftlh",
                "ASK { <http://example.org/operation/123> ?p ?o }",
                () -> operationQueries.checkIfFamOpeSerExists(testUri),
                map -> ("<" + testUri + ">").equals(map.get(Constants.URI))
                        && "\"fr\"".equals(map.get("LG1"))
                        && "\"en\"".equals(map.get("LG2"))
                        && isOperationsGraph(map));
    }

    @Test
    void shouldGetPublicationState() throws RmesException {
        assertQueryBuiltFromTemplate(
                FAM_OPE_SER_FOLDER,
                PUBLICATION_STATUS_TEMPLATE,
                PUBLICATION_STATUS_QUERY,
                () -> operationQueries.getPublicationState("op123"),
                map -> "\"op123\"".equals(map.get(Constants.ID))
                        && "\"fr\"".equals(map.get("LG1"))
                        && "\"en\"".equals(map.get("LG2"))
                        && isOperationsGraph(map));
    }

    @Test
    void shouldRejectANullUriInCheckIfFamOpeSerExists() {
        assertThrows(IllegalArgumentException.class, () -> operationQueries.checkIfFamOpeSerExists(null));
    }

    @Test
    void shouldHandleEmptyIdInGetPublicationState() throws RmesException {
        assertQueryBuiltFromTemplate(
                FAM_OPE_SER_FOLDER,
                PUBLICATION_STATUS_TEMPLATE,
                PUBLICATION_STATUS_QUERY,
                () -> operationQueries.getPublicationState(""),
                map -> "\"\"".equals(map.get(Constants.ID)));
    }

    @Test
    void shouldVerifyInitParamsContainsAllRequiredParameters() throws RmesException {
        callWithStubbedTemplate(
                FAM_OPE_SER_FOLDER,
                LAST_ID_TEMPLATE,
                LAST_ID_QUERY,
                operationQueries::lastId,
                // Verify all required parameters from initParams are present
                map -> map.containsKey("LG1")
                        && map.containsKey("LG2")
                        && map.containsKey("OPERATIONS_GRAPH")
                        && "\"fr\"".equals(map.get("LG1"))
                        && "\"en\"".equals(map.get("LG2"))
                        && isOperationsGraph(map));
    }

    @Test
    void shouldVerifyCorrectTemplatePaths() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker
                    .when(() -> FreeMarkerUtils.buildRequest(eq(FAM_OPE_SER_FOLDER), any(String.class), any(Map.class)))
                    .thenReturn("QUERY_RESULT");

            String lastIdResult = operationQueries.lastId();
            String existsResult = operationQueries.checkIfFamOpeSerExists("http://bauhaus/operations/serie/s1");
            String stateResult = operationQueries.getPublicationState("s1");

            assertEquals("QUERY_RESULT", lastIdResult);
            assertEquals("QUERY_RESULT", existsResult);
            assertEquals("QUERY_RESULT", stateResult);

            // Verify all methods use the same path prefix
            mockedFreeMarker.verify(
                    () -> FreeMarkerUtils.buildRequest(eq(FAM_OPE_SER_FOLDER), eq(LAST_ID_TEMPLATE), any(Map.class)));
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(
                    eq(FAM_OPE_SER_FOLDER), eq("checkIfFamSerOpeExistsQuery.ftlh"), any(Map.class)));
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(
                    eq(FAM_OPE_SER_FOLDER), eq(PUBLICATION_STATUS_TEMPLATE), any(Map.class)));
        }
    }

    @Test
    void shouldVerifyBuildOperationRequestMethod() throws RmesException {
        // Verify that buildOperationRequest uses the correct path (operations/famOpeSer/)
        assertQueryBuiltFromTemplate(
                FAM_OPE_SER_FOLDER, LAST_ID_TEMPLATE, LAST_ID_QUERY, operationQueries::lastId, ANY_PARAMS);
    }

    @Test
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        assertRmesExceptionPropagated(FAM_OPE_SER_FOLDER, LAST_ID_TEMPLATE, operationQueries::lastId);
    }

    private static boolean isOperationsGraph(Map<String, Object> map) {
        return ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH"));
    }
}
