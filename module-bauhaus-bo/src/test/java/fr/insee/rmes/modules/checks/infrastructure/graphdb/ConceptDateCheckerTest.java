package fr.insee.rmes.modules.checks.infrastructure.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.checks.domain.model.CheckResult;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Map;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConceptDateCheckerTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private GraphsProperties graphs;

    private ConceptDateChecker conceptDateChecker;

    @BeforeEach
    void setUp() {
        conceptDateChecker = new ConceptDateChecker(repositoryGestion, graphs);
    }

    private static JSONObject concept(String id, String created, String modified) {
        JSONObject concept = new JSONObject();
        concept.put("id", id);
        concept.put("created", created);
        concept.put("modified", modified);
        return concept;
    }

    private void givenConcepts(JSONArray concepts) throws RmesException {
        when(graphs.conceptsGraph()).thenReturn("http://test.graph");
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(concepts);
    }

    private CheckResult runCheck() {
        Optional<CheckResult> result = conceptDateChecker.check();

        assertThat(result).isPresent();
        return result.get();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> valueOf(CheckResult checkResult) {
        return (Map<String, Object>) checkResult.getValue();
    }

    @Test
    void check_shouldReturnValidResult_whenAllDatesAreValid() throws RmesException {
        // Given
        JSONArray concepts = new JSONArray();
        concepts.put(concept("concept1", "2023-01-15T10:30:00.000Z", "2023-06-20T14:45:30Z"));
        concepts.put(concept("concept2", "2023-02-10T15:30:00Z", "")); // Empty is valid
        givenConcepts(concepts);

        // When
        CheckResult checkResult = runCheck();

        // Then
        assertThat(checkResult.getName()).isEqualTo("ConceptDateChecker");
        assertThat(valueOf(checkResult))
                .containsEntry("status", "completed")
                .containsEntry("totalConcepts", 2)
                .containsEntry("validConcepts", 2)
                .containsEntry("invalidConcepts", 0);
    }

    @Test
    void check_shouldReturnInvalidResult_whenSomeDatesAreInvalid() throws RmesException {
        // Given
        JSONArray concepts = new JSONArray();
        // Valid created date, invalid modified date
        concepts.put(concept("concept1", "2023-01-15T10:30:00.000Z", "invalid-date"));
        // Invalid created date format
        concepts.put(concept("concept2", "2023-02-10 15:30:00", ""));
        givenConcepts(concepts);

        // When
        CheckResult checkResult = runCheck();

        // Then
        assertThat(valueOf(checkResult))
                .containsEntry("status", "completed")
                .containsEntry("totalConcepts", 2)
                .containsEntry("validConcepts", 0)
                .containsEntry("invalidConcepts", 2);
    }

    @Test
    void check_shouldReturnErrorResult_whenExceptionOccurs() throws RmesException {
        // Given
        when(graphs.conceptsGraph()).thenReturn("http://test.graph");
        when(repositoryGestion.getResponseAsArray(anyString())).thenThrow(new RmesException(1, "Database error"));

        // When
        CheckResult checkResult = runCheck();

        // Then
        assertThat(valueOf(checkResult)).containsEntry("status", "error");
    }

    @Test
    void check_shouldHandleEmptyConceptsList() throws RmesException {
        // Given
        givenConcepts(new JSONArray());

        // When
        CheckResult checkResult = runCheck();

        // Then
        assertThat(valueOf(checkResult))
                .containsEntry("status", "completed")
                .containsEntry("totalConcepts", 0)
                .containsEntry("validConcepts", 0)
                .containsEntry("invalidConcepts", 0);
    }

    @Test
    void getSparqlQuery_shouldReturnQueryString() throws RmesException {
        // Given
        when(graphs.conceptsGraph()).thenReturn("http://test.graph");

        // When
        String query = conceptDateChecker.getSparqlQuery();

        // Then
        assertThat(query)
                .isNotNull()
                .contains("SELECT")
                .contains("?id")
                .contains("?created")
                .contains("?modified");
    }
}
