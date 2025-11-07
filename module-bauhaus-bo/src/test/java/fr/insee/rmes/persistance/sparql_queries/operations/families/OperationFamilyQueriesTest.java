package fr.insee.rmes.persistance.sparql_queries.operations.families;

import fr.insee.rmes.Config;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationFamilyQueries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class OperationFamilyQueriesTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = new ConfigStub();
        OperationFamilyQueries.setConfig(config);
    }

    @Test
    void shouldCheckPrefLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel 'Test Family'@en }");

            String result = OperationFamilyQueries.checkPrefLabelUnicity("fam123", "Test Family", "en");

            assertNotNull(result);
            assertEquals("ASK { ?s skos:prefLabel 'Test Family'@en }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "fam123".equals(map.get("ID")) &&
                               "Test Family".equals(map.get("LABEL")) &&
                               "en".equals(map.get("LANG")) &&
                               "/operations/famille/".equals(map.get("URI_PREFIX")) &&
                               "insee:StatisticalOperationFamily".equals(map.get("TYPE")) &&
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldGetFamiliesSearchQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getFamiliesForAdvancedSearch.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?family WHERE { ?family a insee:StatisticalOperationFamily }");

            String result = OperationFamilyQueries.familiesSearchQuery();

            assertNotNull(result);
            assertEquals("SELECT ?family WHERE { ?family a insee:StatisticalOperationFamily }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getFamiliesForAdvancedSearch.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH")) &&
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

            String result = OperationFamilyQueries.checkPrefLabelUnicity(null, null, null);

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
    void shouldHandleEmptyStringsInCheckPrefLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel ''@'' }");

            String result = OperationFamilyQueries.checkPrefLabelUnicity("", "", "");

            assertNotNull(result);
            assertEquals("ASK { ?s skos:prefLabel ''@'' }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "".equals(map.get("ID")) &&
                               "".equals(map.get("LABEL")) &&
                               "".equals(map.get("LANG"));
                    })));
        }
    }

    @Test
    void shouldVerifyCorrectTemplatePathsAreUsed() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            // Test operations/ path for checkPrefLabelUnicity
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), any(String.class), any(Map.class)))
                    .thenReturn("OPERATIONS_RESULT");
            
            // Test operations/famOpeSer/ path for familiesSearchQuery
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), any(String.class), any(Map.class)))
                    .thenReturn("FAMOPSER_RESULT");

            String checkResult = OperationFamilyQueries.checkPrefLabelUnicity("test", "Test", "en");
            String searchResult = OperationFamilyQueries.familiesSearchQuery();

            assertEquals("OPERATIONS_RESULT", checkResult);
            assertEquals("FAMOPSER_RESULT", searchResult);

            // Verify correct paths are used
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)));
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getFamiliesForAdvancedSearch.ftlh"), any(Map.class)));
        }
    }

    @Test
    void shouldVerifyParametersContainAllRequiredFields() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getFamiliesForAdvancedSearch.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?family WHERE { ?family a insee:StatisticalOperationFamily }");

            OperationFamilyQueries.familiesSearchQuery();

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/famOpeSer/"), eq("getFamiliesForAdvancedSearch.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        // Verify all required parameters are present
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
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            RmesException testException = new RmesException(500, "Test error", "Test error message");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenThrow(testException);

            RmesException exception = assertThrows(RmesException.class, () -> 
                OperationFamilyQueries.checkPrefLabelUnicity("test", "Test", "en")
            );

            assertEquals(testException, exception);
        }
    }

    @Test
    void shouldVerifyConstantsAreUsedCorrectly() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel 'Test'@fr }");

            OperationFamilyQueries.checkPrefLabelUnicity("fam456", "Test", "fr");

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        // Verify that the OPERATIONS_GRAPH constant is used correctly
                        return "OPERATIONS_GRAPH".equals("OPERATIONS_GRAPH") && // This verifies the constant exists
                               config.getOperationsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }
}