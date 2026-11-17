package fr.insee.rmes.modules.operations.series.domain.model.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.rmes.modules.operations.series.domain.exceptions.InvalidSeriesCommandException;
import org.junit.jupiter.api.Test;

/**
 * Le libellé principal est le seul que le dépôt écrit sans garde : l'invariant est porté ici, en
 * plus de la validation du corps HTTP, pour qu'un appel qui ne passe pas par le contrôleur ne
 * puisse pas écrire une série sans libellé.
 */
class SeriesCommandTest {

    @Test
    void should_throw_exception_if_pref_label_lg1_is_null() {
        var exception = assertThrows(InvalidSeriesCommandException.class, () -> command(null));
        assertEquals("The prefLabelLg1 is blank", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_pref_label_lg1_is_blank() {
        var exception = assertThrows(InvalidSeriesCommandException.class, () -> command("   "));
        assertEquals("The prefLabelLg1 is blank", exception.getMessage());
    }

    @Test
    void should_accept_a_command_carrying_a_pref_label_lg1() {
        assertDoesNotThrow(() -> command("Série"));
    }

    private static SeriesCommand command(String prefLabelLg1) {
        return new SeriesCommand(
                prefLabelLg1,
                "Series",
                null,
                null,
                null,
                null,
                null,
                null,
                "s60",
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
                "sims-1",
                "2026-09-29");
    }
}
