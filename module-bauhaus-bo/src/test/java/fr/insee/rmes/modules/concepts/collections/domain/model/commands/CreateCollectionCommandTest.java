package fr.insee.rmes.modules.concepts.collections.domain.model.commands;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Lang;
import fr.insee.rmes.modules.concepts.collections.domain.model.LocalisedLabel;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateCollectionCommandTest {

    @Test
    void shouldThrowExceptionIfCreatorIsBlank() throws InvalidCreateCollectionCommandException {
        var exception = assertThrows(InvalidCreateCollectionCommandException.class, () -> {
            new CreateCollectionCommand(
                    new LocalisedLabel("label", Lang.defaultLanguage()),
                    null,
                    Collections.emptyMap(),
                    "",
                    null,
                    Collections.emptyList()
            );
        });

        assertEquals("The creator is blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfAtLeastOneConceptIsBlank() throws InvalidCreateCollectionCommandException {
        var exception = assertThrows(InvalidCreateCollectionCommandException.class, () -> {
            new CreateCollectionCommand(
                    new LocalisedLabel("label", Lang.defaultLanguage()),
                    null,
                    Collections.emptyMap(),
                    "HIE000000",
                    null,
                    List.of("c01011","")
            );
        });

        assertEquals("At least one concept is blank", exception.getMessage());
    }

    @Test
    void shouldCreateNewCreateCollectionCommand() throws InvalidCreateCollectionCommandException {
        assertDoesNotThrow(() -> {
            new CreateCollectionCommand(
                    new LocalisedLabel("label", Lang.defaultLanguage()),
                    null,
                    Collections.emptyMap(),
                    "HIE000000",
                    null,
                    List.of("c01011")
            );
        });
    }
}