package fr.insee.rmes.modules.operations.indicators.domain.model.commands;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorCommandException;
import fr.insee.rmes.modules.operations.indicators.domain.model.OperationLink;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateIndicatorCommandTest {

    private static final List<OperationLink> A_SERIES_LINK = List.of(new OperationLink("s1", "series", List.of()));

    @Test
    void should_throw_exception_if_prefLabels_is_empty() {
        var exception = assertThrows(InvalidIndicatorCommandException.class, () -> new CreateIndicatorCommand(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                A_SERIES_LINK,
                null
        ));

        assertEquals("There are no prefLabels", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_default_prefLabel_is_not_present() {
        var exception = assertThrows(InvalidIndicatorCommandException.class, () -> new CreateIndicatorCommand(
                List.of(LocalisedLabel.ofAlternativeLanguage("value")),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                A_SERIES_LINK,
                null
        ));

        assertEquals("The default prefLabel is not provided", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_wasGeneratedBy_is_empty() {
        var exception = assertThrows(InvalidIndicatorCommandException.class, () -> new CreateIndicatorCommand(
                List.of(LocalisedLabel.ofDefaultLanguage("value")),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        ));

        assertEquals("An indicator should be linked to a series.", exception.getMessage());
    }

    @Test
    void should_create_new_create_indicator_command() {
        assertDoesNotThrow(() -> new CreateIndicatorCommand(
                List.of(LocalisedLabel.ofDefaultLanguage("value")),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                A_SERIES_LINK,
                null
        ));
    }
}
