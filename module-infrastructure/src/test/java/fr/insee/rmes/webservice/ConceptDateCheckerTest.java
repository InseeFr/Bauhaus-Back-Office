package fr.insee.rmes.webservice;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.checks.CheckResult;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptDateCheckerTest {

    @Mock
    private RepositoryGestion repositoryGestion;
    
    @Mock
    private Config config;

    private ConceptDateChecker conceptDateChecker;

    @BeforeEach
    void setUp() {
        conceptDateChecker = new ConceptDateChecker(repositoryGestion, config);
    }

    @Test
    void check_shouldReturnValidResult_whenAllDatesAreValid() throws RmesException {
        // Given
        JSONArray concepts = new JSONArray();
        
        JSONObject concept1 = new JSONObject();
        concept1.put("id", "concept1");
        concept1.put("created", "2023-01-15T10:30:00.000Z");
        concept1.put("modified", "2023-06-20T14:45:30Z");
        concepts.put(concept1);
        
        JSONObject concept2 = new JSONObject();
        concept2.put("id", "concept2");
        concept2.put("created", "2023-02-10T15:30:00Z");
        concept2.put("modified", ""); // Empty is valid
        concepts.put(concept2);

        when(config.getConceptsGraph()).thenReturn("http://test.graph");
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(concepts);

        // When
        Optional<CheckResult> result = conceptDateChecker.check();

        // Then
        assertThat(result).isPresent();
        CheckResult checkResult = result.get();
        assertThat(checkResult.getName()).isEqualTo("ConceptDateChecker");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) checkResult.getValue();
        assertThat(value).containsEntry("status", "completed")
                .containsEntry("totalConcepts", 2)
                .containsEntry("validConcepts", 2)
                .containsEntry("invalidConcepts", 0);
    }

    @Test
    void check_shouldReturnInvalidResult_whenSomeDatesAreInvalid() throws RmesException {
        // Given
        JSONArray concepts = new JSONArray();
        
        JSONObject concept1 = new JSONObject();
        concept1.put("id", "concept1");
        concept1.put("created", "2023-01-15T10:30:00.000Z"); // Valid
        concept1.put("modified", "invalid-date"); // Invalid
        concepts.put(concept1);
        
        JSONObject concept2 = new JSONObject();
        concept2.put("id", "concept2");
        concept2.put("created", "2023-02-10 15:30:00"); // Invalid format
        concept2.put("modified", "");
        concepts.put(concept2);

        when(config.getConceptsGraph()).thenReturn("http://test.graph");
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(concepts);

        // When
        Optional<CheckResult> result = conceptDateChecker.check();

        // Then
        assertThat(result).isPresent();
        CheckResult checkResult = result.get();

        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) checkResult.getValue();
        assertThat(value).containsEntry("status", "completed")
                .containsEntry("totalConcepts", 2)
                .containsEntry("validConcepts", 0)
                .containsEntry("invalidConcepts", 2);

    }

    @Test
    void check_shouldReturnErrorResult_whenExceptionOccurs() throws RmesException {
        // Given
        when(config.getConceptsGraph()).thenReturn("http://test.graph");
        when(repositoryGestion.getResponseAsArray(anyString())).thenThrow(new RmesException(1, "Database error"));

        // When
        Optional<CheckResult> result = conceptDateChecker.check();

        // Then
        assertThat(result).isPresent();
        CheckResult checkResult = result.get();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> value = (Map<String, Object>) checkResult.getValue();
        assertThat(value).containsEntry("status", "error");
    }

    @Test
    void check_shouldHandleEmptyConceptsList() throws RmesException {
        // Given
        JSONArray emptyConcepts = new JSONArray();
        
        when(config.getConceptsGraph()).thenReturn("http://test.graph");
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(emptyConcepts);

        // When
        Optional<CheckResult> result = conceptDateChecker.check();

        // Then
        assertThat(result).isPresent();
        CheckResult checkResult = result.get();

        Map<String, Object> value = (Map<String, Object>) checkResult.getValue();
        assertThat(value).containsEntry("status", "completed")
                .containsEntry("totalConcepts", 0)
                .containsEntry("validConcepts", 0)
                .containsEntry("invalidConcepts", 0);
    }

    @Test
    void getSparqlQuery_shouldReturnQueryString() throws RmesException {
        // Given
        when(config.getConceptsGraph()).thenReturn("http://test.graph");

        // When
        String query = conceptDateChecker.getSparqlQuery();

        // Then
        assertThat(query).isNotNull()
                .contains("SELECT")
                .contains("?id")
                .contains("?created")
                .contains("?modified");
    }
}