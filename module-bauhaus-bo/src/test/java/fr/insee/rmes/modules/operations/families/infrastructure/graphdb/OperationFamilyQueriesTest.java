package fr.insee.rmes.modules.operations.families.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static fr.insee.rmes.persistance.sparql_queries.SparqlQueryNormalizer.normalize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class OperationFamilyQueriesTest {

    private OperationFamilyQueries operationFamilyQueries;
    private final String lg1 = "fr";
    private final String lg2 = "en";
    private final String baseGraph = "http://rdf.insee.fr/graphes/";
    private final String operationsGraph = "operations/";

    @BeforeEach
    void setUp() {
        operationFamilyQueries = new OperationFamilyQueries(new BauhausLanguagesProperties(lg1, lg2), baseGraph, operationsGraph);
    }

    @Test
    void families_query_should_return_query_string() throws RmesException {
        String expectedQuery = "SPARQL QUERY RESULT";

        try (MockedStatic<FreeMarkerUtils> mockedFreeMarkerUtils = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarkerUtils.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenReturn(expectedQuery);

            String result = operationFamilyQueries.familiesQuery();

            assertEquals(expectedQuery, result);
            mockedFreeMarkerUtils.verify(() -> FreeMarkerUtils.buildRequest(
                    eq("operations/famOpeSer/"),
                    eq("getFamilies.ftlh"),
                    any(HashMap.class)
            ));
        }
    }

    @Test
    void families_query_should_throw_rmes_exception_when_free_marker_fails() {
        RmesException expectedException = new RmesException(500, "FreeMarker error", "Details");

        try (MockedStatic<FreeMarkerUtils> mockedFreeMarkerUtils = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarkerUtils.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenThrow(expectedException);

            RmesException thrownException = assertThrows(RmesException.class, () -> operationFamilyQueries.familiesQuery());

            assertEquals(expectedException, thrownException);
        }
    }

    @Test
    void family_query_should_return_query_string() throws RmesException {
        String familyId = "123";
        String expectedQuery = "SPARQL QUERY RESULT";

        try (MockedStatic<FreeMarkerUtils> mockedFreeMarkerUtils = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarkerUtils.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenReturn(expectedQuery);

            String result = operationFamilyQueries.familyQuery(familyId);

            assertEquals(expectedQuery, result);
            mockedFreeMarkerUtils.verify(() -> FreeMarkerUtils.buildRequest(
                    eq("operations/famOpeSer/"),
                    eq("getFamily.ftlh"),
                    any(HashMap.class)
            ));
        }
    }

    @Test
    void family_query_should_throw_rmes_exception_when_free_marker_fails() {
        String familyId = "123";
        RmesException expectedException = new RmesException(500, "FreeMarker error", "Details");

        try (MockedStatic<FreeMarkerUtils> mockedFreeMarkerUtils = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarkerUtils.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenThrow(expectedException);

            RmesException thrownException = assertThrows(RmesException.class, () -> 
                    operationFamilyQueries.familyQuery(familyId));

            assertEquals(expectedException, thrownException);
        }
    }

    @Test
    void get_series_should_return_query_string() throws RmesException {
        String familyId = "456";
        String expectedQuery = "SPARQL QUERY RESULT";

        try (MockedStatic<FreeMarkerUtils> mockedFreeMarkerUtils = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarkerUtils.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenReturn(expectedQuery);

            String result = operationFamilyQueries.getSeries(familyId);

            assertEquals(expectedQuery, result);
            mockedFreeMarkerUtils.verify(() -> FreeMarkerUtils.buildRequest(
                    eq("operations/famOpeSer/"),
                    eq("getSeries.ftlh"),
                    any(HashMap.class)
            ));
        }
    }

    @Test
    void get_series_should_throw_rmes_exception_when_free_marker_fails() {
        String familyId = "456";
        RmesException expectedException = new RmesException(500, "FreeMarker error", "Details");

        try (MockedStatic<FreeMarkerUtils> mockedFreeMarkerUtils = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarkerUtils.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenThrow(expectedException);

            RmesException thrownException = assertThrows(RmesException.class, () -> 
                    operationFamilyQueries.getSeries(familyId));

            assertEquals(expectedException, thrownException);
        }
    }

    @Test
    void get_subjects_should_return_sparql_query() throws RmesException {
        String familyId = "789";

        String result = normalize(operationFamilyQueries.getSubjects(familyId));

        assertEquals(normalize("""
                SELECT  ?id ?labelLg1 ?labelLg2
                 FROM <http://rdf.insee.fr/graphes/operations/>
                WHERE {
                ?family dcterms:subject ?subjectUri .
                ?subjectUri skos:prefLabel ?labelLg1 .
                FILTER (lang(?labelLg1) = "fr") .
                ?subjectUri skos:prefLabel ?labelLg2 .
                FILTER (lang(?labelLg2) = "en") .
                ?subjectUri skos:notation ?id .
                FILTER(STRENDS(STR(?family),\"/operations/famille/789\")) .
                } ORDER BY ?subjectUri
                """), result);
    }

    @Test
    void get_subjects_should_contain_correct_family_id_in_filter() throws RmesException {
        String familyId = "test-family-123";

        String result = operationFamilyQueries.getSubjects(familyId);

        assertTrue(result.contains("/operations/famille/" + familyId));
    }

    @Test
    void constructor_should_initialize_all_fields() throws RmesException {
        String testLg1 = "test-lg1";
        String testLg2 = "test-lg2";
        String testBaseGraph = "http://test-base/";
        String testOperationsGraph = "test-operations/";

        OperationFamilyQueries queries = new OperationFamilyQueries(new BauhausLanguagesProperties(testLg1, testLg2), testBaseGraph, testOperationsGraph);

        String subjectsQuery = queries.getSubjects("test-id");
        assertTrue(subjectsQuery.contains("FILTER (lang(?labelLg1) = \"" + testLg1 + "\")"));
        assertTrue(subjectsQuery.contains("FILTER (lang(?labelLg2) = \"" + testLg2 + "\")"));
        assertTrue(subjectsQuery.contains("FROM <" + testBaseGraph + testOperationsGraph + ">"));
    }

    @Test
    void check_pref_label_unicity_should_build_the_ask_query_for_the_requested_language() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s skos:prefLabel 'Test Family'@fr }");

            String result = operationFamilyQueries.checkPrefLabelUnicity("fam123", "Test Family", Language.lg1);

            assertEquals("ASK { ?s skos:prefLabel 'Test Family'@fr }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"),
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "\"Test Family\"@fr".equals(map.get("LABEL")) &&
                               "\"/operations/famille/fam123\"".equals(map.get("URI_SUFFIX")) &&
                               "insee:StatisticalOperationFamily".equals(map.get("TYPE")) &&
                               ("<" + baseGraph + operationsGraph + ">").equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void check_pref_label_unicity_should_tag_the_label_with_lg2_when_asked_for_lg2() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenReturn("ASK {}");

            operationFamilyQueries.checkPrefLabelUnicity("fam123", "Test Family", Language.lg2);

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(),
                    argThat(params -> "\"Test Family\"@en".equals(((Map<String, Object>) params).get("LABEL")))));
        }
    }

    @Test
    void check_pref_label_unicity_should_reject_null_values() {
        assertThrows(IllegalArgumentException.class,
                () -> operationFamilyQueries.checkPrefLabelUnicity(null, null, Language.lg1));
    }
}
