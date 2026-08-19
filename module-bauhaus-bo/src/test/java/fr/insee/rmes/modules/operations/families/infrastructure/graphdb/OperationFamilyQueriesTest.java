package fr.insee.rmes.modules.operations.families.infrastructure.graphdb;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        operationFamilyQueries = new OperationFamilyQueries(lg1, lg2, baseGraph, operationsGraph);
    }

    @Test
    void familiesQuery_ShouldReturnQueryString() throws RmesException {
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
    void familiesQuery_ShouldThrowRmesExceptionWhenFreeMarkerFails() {
        RmesException expectedException = new RmesException(500, "FreeMarker error", "Details");

        try (MockedStatic<FreeMarkerUtils> mockedFreeMarkerUtils = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarkerUtils.when(() -> FreeMarkerUtils.buildRequest(anyString(), anyString(), any(Map.class)))
                    .thenThrow(expectedException);

            RmesException thrownException = assertThrows(RmesException.class, () -> operationFamilyQueries.familiesQuery());

            assertEquals(expectedException, thrownException);
        }
    }

    @Test
    void familyQuery_ShouldReturnQueryString() throws RmesException {
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
    void familyQuery_ShouldThrowRmesExceptionWhenFreeMarkerFails() {
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
    void getSeries_ShouldReturnQueryString() throws RmesException {
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
    void getSeries_ShouldThrowRmesExceptionWhenFreeMarkerFails() {
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
    void getSubjects_ShouldReturnSparqlQuery() throws RmesException {
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
    void getSubjects_ShouldContainCorrectFamilyIdInFilter() throws RmesException {
        String familyId = "test-family-123";

        String result = operationFamilyQueries.getSubjects(familyId);

        assertTrue(result.contains("/operations/famille/" + familyId));
    }

    @Test
    void constructor_ShouldInitializeAllFields() throws RmesException {
        String testLg1 = "test-lg1";
        String testLg2 = "test-lg2";
        String testBaseGraph = "http://test-base/";
        String testOperationsGraph = "test-operations/";

        OperationFamilyQueries queries = new OperationFamilyQueries(testLg1, testLg2, testBaseGraph, testOperationsGraph);

        String subjectsQuery = queries.getSubjects("test-id");
        assertTrue(subjectsQuery.contains("FILTER (lang(?labelLg1) = \"" + testLg1 + "\")"));
        assertTrue(subjectsQuery.contains("FILTER (lang(?labelLg2) = \"" + testLg2 + "\")"));
        assertTrue(subjectsQuery.contains("FROM <" + testBaseGraph + testOperationsGraph + ">"));
    }

    private static String normalize(String sparql) {
        return sparql.replaceAll("\\s+", " ").trim();
    }
}