package fr.insee.rmes.modules.concepts.collections.domain.model;


import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedLocalisedLabelException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalisedLabelTest {

    @ParameterizedTest
    @ValueSource(strings = {"","  "})
    void shouldThrowExceptionIfValueIsBlank(String blankString){
        assertThrows(MalformedLocalisedLabelException.class, () -> LocalisedLabel.ofDefaultLanguage(blankString));
    }

    @Test
    void should_throw_exception_if_value_is_null(){
        assertThrows(MalformedLocalisedLabelException.class, () -> LocalisedLabel.ofDefaultLanguage(null));
    }

    @Test
    void should_create_new_localised_label(){
        assertDoesNotThrow(() -> LocalisedLabel.ofDefaultLanguage("Label"));
    }
}