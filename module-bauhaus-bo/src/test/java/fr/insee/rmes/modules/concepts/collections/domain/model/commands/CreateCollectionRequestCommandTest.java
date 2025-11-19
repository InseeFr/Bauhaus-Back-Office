package fr.insee.rmes.modules.concepts.collections.domain.model.commands;

import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateCollectionRequestCommandTest {

    @Test
    void should_throw_exception_if_labels_is_empty() throws InvalidCreateCollectionCommandException {
        var exception = assertThrows(InvalidCreateCollectionCommandException.class, () -> {
            new CreateCollectionCommand(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "HIE000000",
                    null,
                    List.of("c01011")
            );
        });

        assertEquals("There are no labels", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_default_label_is_not_present() throws InvalidCreateCollectionCommandException {
        var exception = assertThrows(InvalidCreateCollectionCommandException.class, () -> {
            new CreateCollectionCommand(
                    List.of(LocalisedLabel.ofAlternativeLanguage("value")),
                    Collections.emptyList(),
                    "HIE000000",
                    null,
                    List.of("c01011")
            );
        });

        assertEquals("The default label is not provided", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_creator_is_blank() throws InvalidCreateCollectionCommandException {
        var exception = assertThrows(InvalidCreateCollectionCommandException.class, () -> {
            new CreateCollectionCommand(
                    List.of(LocalisedLabel.ofDefaultLanguage("value")),
                    Collections.emptyList(),
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
                    List.of(LocalisedLabel.ofDefaultLanguage("value")),
                    Collections.emptyList(),
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
                    List.of(LocalisedLabel.ofDefaultLanguage("value")),
                    Collections.emptyList(),
                    "HIE000000",
                    null,
                    List.of("c01011")
            );
        });
    }
}