package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConceptIdTest {

    @Test
    void should_reject_null_value() {
        assertThatThrownBy(() -> new ConceptId(null))
                .isInstanceOf(InvalidConceptIdException.class)
                .hasMessage("The identifier is null");
    }

    @Test
    void should_reject_empty_value() {
        assertThatThrownBy(() -> new ConceptId(""))
                .isInstanceOf(InvalidConceptIdException.class)
                .hasMessage("The identifier is empty");
    }

    /**
     * Le pattern d'ID est appliqué à la frontière HTTP (CreateConceptRequest), pas dans le
     * value object lui-même : les concepts legacy déjà persistés avec un format atypique
     * doivent rester lisibles et modifiables. ConceptId tolère donc tout caractère non-vide.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "c00001",
            "c1",
            "concept-001",
            "underscore_x",
            "dot.value",
            "café",
            "with space"
    })
    void should_accept_any_non_blank_value(String value) {
        assertDoesNotThrow(() -> new ConceptId(value));
    }

    @Test
    void should_expose_value() {
        assertThat(new ConceptId("c00001").value()).isEqualTo("c00001");
    }
}
