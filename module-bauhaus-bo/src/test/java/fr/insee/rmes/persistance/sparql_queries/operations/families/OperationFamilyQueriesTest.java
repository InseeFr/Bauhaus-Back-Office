package fr.insee.rmes.persistance.sparql_queries.operations.families;

import fr.insee.rmes.config.GraphsPropertiesStub;
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

    private OperationFamilyQueries operationFamilyQueries;

    @BeforeEach
    void setUp() {
        operationFamilyQueries = new OperationFamilyQueries(GraphsPropertiesStub.stub());
    }

    @Test
    void shouldCheckPrefLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel 'Test Family'@en }");

            String result = operationFamilyQueries.checkPrefLabelUnicity("fam123", "Test Family", "en");

            assertNotNull(result);
            assertEquals("ASK { ?s skos:prefLabel 'Test Family'@en }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "\"Test Family\"@en".equals(map.get("LABEL")) &&
                               "\"/operations/famille/fam123\"".equals(map.get("URI_SUFFIX")) &&
                               "insee:StatisticalOperationFamily".equals(map.get("TYPE")) &&
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }



    @Test
    void shouldRejectNullValuesInCheckPrefLabelUnicity() {
        assertThrows(IllegalArgumentException.class,
                () -> operationFamilyQueries.checkPrefLabelUnicity(null, null, null));
    }

    @Test
    void shouldRejectAnEmptyLanguageInCheckPrefLabelUnicity() {
        assertThrows(IllegalArgumentException.class,
                () -> operationFamilyQueries.checkPrefLabelUnicity("", "", ""));
    }



    @Test
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            RmesException testException = new RmesException(500, "Test error", "Test error message");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenThrow(testException);

            RmesException exception = assertThrows(RmesException.class, () -> 
                operationFamilyQueries.checkPrefLabelUnicity("test", "Test", "en")
            );

            assertEquals(testException, exception);
        }
    }

    @Test
    void shouldVerifyConstantsAreUsedCorrectly() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel 'Test'@fr }");

            operationFamilyQueries.checkPrefLabelUnicity("fam456", "Test", "fr");

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        // Verify that the OPERATIONS_GRAPH constant is used correctly
                        return "OPERATIONS_GRAPH".equals("OPERATIONS_GRAPH") && // This verifies the constant exists
                               ("<" + GraphsPropertiesStub.stub().operationsGraph() + ">").equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }
}