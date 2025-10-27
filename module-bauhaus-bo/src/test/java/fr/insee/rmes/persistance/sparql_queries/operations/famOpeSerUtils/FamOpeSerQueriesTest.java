package fr.insee.rmes.persistance.sparql_queries.operations.famOpeSerUtils;

import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class FamOpeSerQueriesTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = new ConfigStub();
        FamOpeSerQueries.setConfig(config);
    }

    @Test
    void shouldGetLastId() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?lastId WHERE { ?s dcterms:identifier ?lastId }");

            String result = FamOpeSerQueries.lastId();

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?s dcterms:identifier ?lastId }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldCheckIfFamOpeSerExists() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("checkIfFamSerOpeExistsQuery.ftlh"), any(Map.class)))
                    .thenReturn("ASK { <http://example.org/operation/123> ?p ?o }");

            String testUri = "http://example.org/operation/123";
            String result = FamOpeSerQueries.checkIfFamOpeSerExists(testUri);

            assertNotNull(result);
            assertEquals("ASK { <http://example.org/operation/123> ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("checkIfFamSerOpeExistsQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return testUri.equals(map.get(Constants.URI)) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldGetPublicationState() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getPublicationStatusQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?state WHERE { ?s insee:validationState ?state }");

            String result = FamOpeSerQueries.getPublicationState("op123");

            assertNotNull(result);
            assertEquals("SELECT ?state WHERE { ?s insee:validationState ?state }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getPublicationStatusQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "op123".equals(map.get(Constants.ID)) &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldHandleNullUriInCheckIfFamOpeSerExists() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("checkIfFamSerOpeExistsQuery.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s ?p ?o }");

            String result = FamOpeSerQueries.checkIfFamOpeSerExists(null);

            assertNotNull(result);
            assertEquals("ASK { ?s ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("checkIfFamSerOpeExistsQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return map.get(Constants.URI) == null &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldHandleEmptyIdInGetPublicationState() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getPublicationStatusQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?state WHERE { ?s insee:validationState ?state }");

            String result = FamOpeSerQueries.getPublicationState("");

            assertNotNull(result);
            assertEquals("SELECT ?state WHERE { ?s insee:validationState ?state }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getPublicationStatusQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "".equals(map.get(Constants.ID));
                    })));
        }
    }

    @Test
    void shouldVerifyInitParamsContainsAllRequiredParameters() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?lastId WHERE { ?s dcterms:identifier ?lastId }");

            FamOpeSerQueries.lastId();

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        // Verify all required parameters from initParams are present
                        return map.containsKey("LG1") &&
                               map.containsKey("LG2") &&
                               map.containsKey("OPERATIONS_GRAPH") &&
                               config.getLg1().equals(map.get("LG1")) &&
                               config.getLg2().equals(map.get("LG2")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldVerifyCorrectTemplatePaths() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), any(String.class), any(Map.class)))
                    .thenReturn("QUERY_RESULT");

            String lastIdResult = FamOpeSerQueries.lastId();
            String existsResult = FamOpeSerQueries.checkIfFamOpeSerExists("test");
            String stateResult = FamOpeSerQueries.getPublicationState("test");

            assertEquals("QUERY_RESULT", lastIdResult);
            assertEquals("QUERY_RESULT", existsResult);
            assertEquals("QUERY_RESULT", stateResult);

            // Verify all methods use the same path prefix
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), any(Map.class)));
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("checkIfFamSerOpeExistsQuery.ftlh"), any(Map.class)));
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getPublicationStatusQuery.ftlh"), any(Map.class)));
        }
    }

    @Test
    void shouldVerifyBuildOperationRequestMethod() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?lastId WHERE { ?s dcterms:identifier ?lastId }");

            String result = FamOpeSerQueries.lastId();

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?s dcterms:identifier ?lastId }", result);
            
            // Verify that buildOperationRequest uses the correct path (operations/famOpeSer/)
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), any(Map.class)));
        }
    }

    @Test
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            RmesException testException = new RmesException(500, "Test error", "Test error message");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getLastIdQuery.ftlh"), any(Map.class)))
                    .thenThrow(testException);

            RmesException exception = assertThrows(RmesException.class, FamOpeSerQueries::lastId
            );

            assertEquals(testException, exception);
        }
    }

}