package fr.insee.rmes.modules.concepts.concept.domain.model.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateConceptCommandTest {

    private static final String DISSEMINATION_STATUS = "http://id.insee.fr/codes/base/statutDiffusion/Prive";

    @Test
    void should_throw_exception_if_labels_is_empty() {
        var exception = assertThrows(
                InvalidCreateConceptCommandException.class,
                () -> new CreateConceptCommand(
                        Collections.emptyList(), "HIE000000", null, DISSEMINATION_STATUS, Collections.emptyList()));
        assertEquals("There are no labels", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_default_label_is_not_present() {
        var exception = assertThrows(
                InvalidCreateConceptCommandException.class,
                () -> new CreateConceptCommand(
                        List.of(LocalisedLabel.ofAlternativeLanguage("only english")),
                        "HIE000000",
                        null,
                        DISSEMINATION_STATUS,
                        Collections.emptyList()));
        assertEquals("The default label is not provided", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_creator_is_blank() {
        var exception = assertThrows(
                InvalidCreateConceptCommandException.class,
                () -> new CreateConceptCommand(
                        List.of(LocalisedLabel.ofDefaultLanguage("value")),
                        "  ",
                        null,
                        DISSEMINATION_STATUS,
                        Collections.emptyList()));
        assertEquals("The creator is blank", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_dissemination_status_is_blank() {
        var exception = assertThrows(
                InvalidCreateConceptCommandException.class,
                () -> new CreateConceptCommand(
                        List.of(LocalisedLabel.ofDefaultLanguage("value")),
                        "HIE000000",
                        null,
                        "",
                        Collections.emptyList()));
        assertEquals("The dissemination status is blank", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_at_least_one_collection_id_is_blank() {
        var exception = assertThrows(
                InvalidCreateConceptCommandException.class,
                () -> new CreateConceptCommand(
                        List.of(LocalisedLabel.ofDefaultLanguage("value")),
                        "HIE000000",
                        null,
                        DISSEMINATION_STATUS,
                        List.of("Collection-001", " ")));
        assertEquals("At least one collection identifier is blank", exception.getMessage());
    }

    @Test
    void should_build_valid_command_with_minimal_payload() {
        assertDoesNotThrow(() -> new CreateConceptCommand(
                List.of(LocalisedLabel.ofDefaultLanguage("Mon concept")),
                "HIE000000",
                null,
                DISSEMINATION_STATUS,
                Collections.emptyList()));
    }
}
