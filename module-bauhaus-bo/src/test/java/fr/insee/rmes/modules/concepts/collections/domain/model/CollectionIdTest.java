package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CollectionIdTest {

    @Test
    void should_reject_null_value() {
        assertThatThrownBy(() -> new CollectionId(null))
                .isInstanceOf(InvalidCollectionIdException.class)
                .hasMessage("The identifier is null");
    }

    @Test
    void should_reject_empty_value() {
        assertThatThrownBy(() -> new CollectionId(""))
                .isInstanceOf(InvalidCollectionIdException.class)
                .hasMessage("The identifier is empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "café",          // accents
            "en–dash",       // tiret demi-cadratin
            "em—dash",       // tiret cadratin
            "with space",    // espace
            "comma,sep",     // ponctuation
            "slash/here",    // séparateur d'URI
            "underscore_x",  // underscore non autorisé
            "dot.value",     // point non autorisé
            "résumé"         // accents multiples
    })
    void should_reject_value_with_forbidden_characters(String invalidValue) {
        assertThatThrownBy(() -> new CollectionId(invalidValue))
                .isInstanceOf(InvalidCollectionIdException.class)
                .hasMessageContaining("invalid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abc",
            "ABC",
            "abc123",
            "ABC-123",
            "a-b-c",
            "1234",
            "Collection-001",
            "550e8400-e29b-41d4-a716-446655440000" // UUID — compatibilité héritée
    })
    void should_accept_alphanumeric_and_hyphen_only(String validValue) {
        assertDoesNotThrow(() -> new CollectionId(validValue));
    }

    @Test
    void should_expose_value() {
        assertThat(new CollectionId("Collection-001").value()).isEqualTo("Collection-001");
    }
}
