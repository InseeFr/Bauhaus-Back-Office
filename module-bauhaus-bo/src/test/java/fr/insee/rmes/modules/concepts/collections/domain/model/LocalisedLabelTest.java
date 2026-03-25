package fr.insee.rmes.modules.concepts.collections.domain.model;


import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedLocalisedLabelException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalisedLabelTest {

    @Test
    void should_create_new_localised_label(){
        assertDoesNotThrow(() -> LocalisedLabel.ofDefaultLanguage("Label"));
    }
}