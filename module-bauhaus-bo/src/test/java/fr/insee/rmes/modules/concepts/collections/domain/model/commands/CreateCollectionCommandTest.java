package fr.insee.rmes.modules.concepts.collections.domain.model.commands;

import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateCollectionCommandTest {

    @Test
    void should_throw_exception_if_creator_is_blank() throws InvalidCreateCollectionCommandException {
        var exception = assertThrows(InvalidCreateCollectionCommandException.class, () -> {
            new CreateCollectionCommand(
                    LocalisedLabel.ofDefaultLanguage("value"),
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
    void should_throw_exception_if_at_least_one_concept_is_blank() throws InvalidCreateCollectionCommandException {
        var exception = assertThrows(InvalidCreateCollectionCommandException.class, () -> {
            new CreateCollectionCommand(
                    LocalisedLabel.ofDefaultLanguage("value"),
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
    void should_create_new_create_collection_command() throws InvalidCreateCollectionCommandException {
        assertDoesNotThrow(() -> {
            new CreateCollectionCommand(
                   LocalisedLabel.ofDefaultLanguage("value"),
                    null,
                    Collections.emptyMap(),
                    "HIE000000",
                    null,
                    List.of("c01011")
            );
        });
    }
}