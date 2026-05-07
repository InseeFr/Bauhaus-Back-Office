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

    /**
     * Le pattern d'ID est appliqué uniquement à la frontière HTTP (CreateCollectionRequest)
     * pour ne pas casser la lecture des collections legacy déjà persistées avec un ID
     * non conforme (underscore, etc.). CollectionId tolère donc tout caractère côté domaine.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "abc",
            "Collection-001",
            "underscore_x",  // legacy
            "dot.value",     // legacy
            "café",          // legacy avec accents
            "with space"     // legacy avec espace
    })
    void should_accept_any_non_blank_value(String value) {
        assertDoesNotThrow(() -> new CollectionId(value));
    }

    @Test
    void should_expose_value() {
        assertThat(new CollectionId("Collection-001").value()).isEqualTo("Collection-001");
    }
}
