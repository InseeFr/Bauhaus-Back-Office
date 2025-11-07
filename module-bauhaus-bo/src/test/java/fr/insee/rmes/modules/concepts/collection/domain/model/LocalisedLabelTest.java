package fr.insee.rmes.modules.concepts.collection.domain.model;


import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.MalformedLocalisedLabelException;
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
    void shouldThrowExceptionIfValueIsNull(){
        assertThrows(MalformedLocalisedLabelException.class, () -> LocalisedLabel.ofDefaultLanguage(null));
    }

    @Test
    void shouldCreateNewLocalisedLabel(){
        assertDoesNotThrow(() -> LocalisedLabel.ofDefaultLanguage("Label"));
    }
}