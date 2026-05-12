package fr.insee.rmes.modules.concepts.concept.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConceptVersionTest {

    @Test
    void initial_concept_version_is_one() {
        assertThat(ConceptVersion.initial().value()).isEqualTo(1);
    }

    @Test
    void next_increments_the_version() {
        assertThat(new ConceptVersion(3).next().value()).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -42})
    void should_reject_zero_or_negative_values(int invalid) {
        assertThatThrownBy(() -> new ConceptVersion(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("concept version must be >= 1");
    }
}
