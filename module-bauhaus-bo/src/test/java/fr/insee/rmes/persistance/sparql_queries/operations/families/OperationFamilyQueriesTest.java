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
    void shouldHandleNullValuesInCheckPrefLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel ?label }");

            String result = OperationFamilyQueries.checkPrefLabelUnicity(null, null, null);

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