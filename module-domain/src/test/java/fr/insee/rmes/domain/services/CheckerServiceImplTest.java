package fr.insee.rmes.domain.services;

import fr.insee.rmes.domain.model.checks.CheckResult;
import fr.insee.rmes.domain.port.serverside.RuleChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckerServiceImplTest {

    @Mock
    private RuleChecker checker1;
    
    @Mock
    private RuleChecker checker2;
    
    @Mock
    private RuleChecker checker3;

    private CheckerServiceImpl checkerService;

    @BeforeEach
    void setUp() {
        checkerService = new CheckerServiceImpl(List.of(checker1, checker2, checker3));
    }

    @Test
    void checks_shouldReturnAllResults_whenAllCheckersReturnResults() {
        // Given
        CheckResult result1 = new CheckResult("check1", "value1");
        CheckResult result2 = new CheckResult("check2", "value2");
        CheckResult result3 = new CheckResult("check3", "value3");
        
        when(checker1.check()).thenReturn(Optional.of(result1));
        when(checker2.check()).thenReturn(Optional.of(result2));
        when(checker3.check()).thenReturn(Optional.of(result3));

        // When
        List<CheckResult> results = checkerService.checks();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).containsExactly(result1, result2, result3);
    }

    @Test
    void checks_shouldFilterEmptyResults() {
        // Given
        CheckResult result1 = new CheckResult("check1", "value1");
        CheckResult result3 = new CheckResult("check3", "value3");
        
        when(checker1.check()).thenReturn(Optional.of(result1));
        when(checker2.check()).thenReturn(Optional.empty());
        when(checker3.check()).thenReturn(Optional.of(result3));

        // When
        List<CheckResult> results = checkerService.checks();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(result1, result3);
    }

    @Test
    void checks_shouldHandleExceptions_andReturnErrorResults() {
        // Given
        CheckResult result1 = new CheckResult("check1", "value1");
        
        when(checker1.check()).thenReturn(Optional.of(result1));
        when(checker2.check()).thenThrow(new RuntimeException("Test exception"));
        when(checker3.check()).thenReturn(Optional.empty());

        // When
        List<CheckResult> results = checkerService.checks();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(result1);
        
        CheckResult errorResult = results.get(1);
        assertThat(errorResult.getName()).startsWith("error_");
        assertThat(errorResult.getName()).containsIgnoringCase("rulechecker");
        assertThat(errorResult.getValue()).asString().contains("Check failed: Test exception");
    }

    @Test
    void checks_shouldHandleEmptyCheckersList() {
        // Given
        checkerService = new CheckerServiceImpl(List.of());

        // When
        List<CheckResult> results = checkerService.checks();

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void checks_shouldHandleNullCheckersList() {
        // Given
        checkerService = new CheckerServiceImpl(null);

        // When
        List<CheckResult> results = checkerService.checks();

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void checks_shouldReturnEmptyList_whenAllCheckersReturnEmpty() {
        // Given
        when(checker1.check()).thenReturn(Optional.empty());
        when(checker2.check()).thenReturn(Optional.empty());
        when(checker3.check()).thenReturn(Optional.empty());

        // When
        List<CheckResult> results = checkerService.checks();

        // Then
        assertThat(results).isEmpty();
    }
}