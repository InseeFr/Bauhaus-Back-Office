package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.model.checks.CheckResult;
import fr.insee.rmes.domain.port.clientside.CheckerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecksResourcesTest {

    @Mock
    private CheckerService checkerService;

    private ChecksResources checksResources;

    @BeforeEach
    void setUp() {
        checksResources = new ChecksResources(checkerService);
    }

    @Test
    void runAllChecks_shouldReturnOk_whenChecksSucceed() {
        // Given
        CheckResult result1 = new CheckResult("check1", "value1");
        CheckResult result2 = new CheckResult("check2", "value2");
        List<CheckResult> expectedResults = List.of(result1, result2);
        
        when(checkerService.checks()).thenReturn(expectedResults);

        // When
        ResponseEntity<List<CheckResult>> response = checksResources.runAllChecks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResults);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void runAllChecks_shouldReturnEmptyList_whenNoChecksFound() {
        // Given
        when(checkerService.checks()).thenReturn(List.of());

        // When
        ResponseEntity<List<CheckResult>> response = checksResources.runAllChecks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void runAllChecks_shouldReturnError_whenServiceThrowsException() {
        // Given
        when(checkerService.checks()).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<CheckResult>> response = checksResources.runAllChecks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).hasSize(1);
        
        CheckResult errorResult = response.getBody().get(0);
        assertThat(errorResult.getName()).isEqualTo("system_error");
        assertThat(errorResult.getValue()).asString().contains("Failed to execute checks: Service error");
    }

    @Test
    void runAllChecks_shouldReturnError_whenServiceThrowsNullPointerException() {
        // Given
        when(checkerService.checks()).thenThrow(new NullPointerException("Null pointer"));

        // When
        ResponseEntity<List<CheckResult>> response = checksResources.runAllChecks();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).hasSize(1);
        
        CheckResult errorResult = response.getBody().get(0);
        assertThat(errorResult.getName()).isEqualTo("system_error");
        assertThat(errorResult.getValue()).asString().contains("Null pointer");
    }
}