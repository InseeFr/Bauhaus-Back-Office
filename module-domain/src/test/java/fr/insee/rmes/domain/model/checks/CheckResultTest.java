package fr.insee.rmes.domain.model.checks;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CheckResultTest {

    @Test
    void constructor_shouldSetNameAndValue() {
        // Given
        String name = "test_check";
        String value = "test_value";

        // When
        CheckResult result = new CheckResult(name, value);

        // Then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getValue()).isEqualTo(value);
    }

    @Test
    void constructor_shouldHandleNullValue() {
        // Given
        String name = "test_check";

        // When
        CheckResult result = new CheckResult(name, null);

        // Then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getValue()).isNull();
    }

    @Test
    void constructor_shouldHandleComplexValue() {
        // Given
        String name = "complex_check";
        Map<String, Object> complexValue = Map.of(
                "status", "success",
                "count", 42,
                "details", Map.of("sub", "value")
        );

        // When
        CheckResult result = new CheckResult(name, complexValue);

        // Then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getValue()).isEqualTo(complexValue);
    }

    @Test
    void equals_shouldReturnTrue_forSameNameAndValue() {
        // Given
        CheckResult result1 = new CheckResult("name", "value");
        CheckResult result2 = new CheckResult("name", "value");

        // When & Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).hasSameHashCodeAs(result2);
    }

    @Test
    void equals_shouldReturnFalse_forDifferentName() {
        // Given
        CheckResult result1 = new CheckResult("name1", "value");
        CheckResult result2 = new CheckResult("name2", "value");

        // When & Then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void equals_shouldReturnFalse_forDifferentValue() {
        // Given
        CheckResult result1 = new CheckResult("name", "value1");
        CheckResult result2 = new CheckResult("name", "value2");

        // When & Then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void equals_shouldHandleNullValues() {
        // Given
        CheckResult result1 = new CheckResult("name", null);
        CheckResult result2 = new CheckResult("name", null);
        CheckResult result3 = new CheckResult("name", "value");

        // When & Then
        assertThat(result1).isEqualTo(result2).isNotEqualTo(result3);
        assertThat(result3).isNotEqualTo(result1);
    }

    @Test
    void equals_shouldReturnFalse_forNull() {
        // Given
        CheckResult result = new CheckResult("name", "value");

        // When & Then
        assertThat(result).isNotEqualTo(null);
    }

    @Test
    void equals_shouldReturnFalse_forDifferentType() {
        // Given
        CheckResult result = new CheckResult("name", "value");

        // When & Then
        assertThat(result).isNotEqualTo("some string");
    }

    @Test
    void equals_shouldReturnTrue_forSameInstance() {
        // Given
        CheckResult result = new CheckResult("name", "value");

        // When & Then
        assertThat(result.equals(result)).isTrue();
    }

    @Test
    void toString_shouldContainNameAndValue() {
        // Given
        CheckResult result = new CheckResult("test_name", "test_value");

        // When
        String toString = result.toString();

        // Then
        assertThat(toString).contains("test_name").contains("test_value").contains("CheckResult");
    }

    @Test
    void toString_shouldHandleNullValue() {
        // Given
        CheckResult result = new CheckResult("test_name", null);

        // When
        String toString = result.toString();

        // Then
        assertThat(toString).contains("test_name").contains("null");
    }
}