package fr.insee.rmes.modules.operations.indicators.domain.model.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorCommandException;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorLink;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Le libellé principal est le seul que le dépôt écrit sans garde : l'invariant est porté ici, en
 * plus de la validation du corps HTTP, pour qu'un appel qui ne passe pas par le contrôleur ne
 * puisse pas écrire un indicateur sans libellé.
 */
class IndicatorCommandTest {

    @Test
    void should_throw_exception_if_pref_label_lg1_is_null() {
        var exception = assertThrows(InvalidIndicatorCommandException.class, () -> command(null));
        assertEquals("The prefLabelLg1 is blank", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_pref_label_lg1_is_blank() {
        var exception = assertThrows(InvalidIndicatorCommandException.class, () -> command("   "));
        assertEquals("The prefLabelLg1 is blank", exception.getMessage());
    }

    @Test
    void should_accept_a_command_carrying_a_pref_label_lg1() {
        assertDoesNotThrow(() -> command("Indicateur"));
    }

    private static IndicatorCommand command(String prefLabelLg1) {
        return new IndicatorCommand(
                prefLabelLg1,
                "Indicator",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new IndicatorLink("s1", "series")),
                "sims-1",
                "2026-09-23",
                null);
    }
}
