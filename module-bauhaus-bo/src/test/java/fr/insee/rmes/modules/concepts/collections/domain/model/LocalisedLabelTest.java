package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LocalisedLabelTest {

    @Test
    void should_create_new_localised_label(){
        assertDoesNotThrow(() -> LocalisedLabel.ofDefaultLanguage("Label"));
    }
}