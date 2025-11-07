package fr.insee.rmes.modules.concepts.collections.domain.model;


import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedLocalisedLabelException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalisedLabelTest {

    @Test
    void shouldThrowExceptionIfValueIsBlank(){
        assertThrows(MalformedLocalisedLabelException.class, () -> new LocalisedLabel("", Lang.defaultLanguage()));
    }

    @Test
    void shouldCreateNewLocalisedLabel(){
        assertDoesNotThrow(() -> new LocalisedLabel("Label", Lang.defaultLanguage()));
    }
}