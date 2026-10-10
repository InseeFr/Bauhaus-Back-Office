package fr.insee.rmes.modules.operations.operations.domain.model.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.rmes.modules.operations.operations.domain.exceptions.InvalidOperationCommandException;
import org.junit.jupiter.api.Test;

/**
 * Le libellé principal est le seul que le dépôt écrit sans garde : l'invariant est porté ici, en
 * plus de la validation du corps HTTP, pour qu'un appel qui ne passe pas par le contrôleur ne
 * puisse pas écrire une opération sans libellé.
 */
class OperationCommandTest {

    @Test
    void should_throw_exception_if_pref_label_lg1_is_null() {
        var exception = assertThrows(InvalidOperationCommandException.class, () -> command(null));
        assertEquals("The prefLabelLg1 is blank", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_pref_label_lg1_is_blank() {
        var exception = assertThrows(InvalidOperationCommandException.class, () -> command("   "));
        assertEquals("The prefLabelLg1 is blank", exception.getMessage());
    }

    @Test
    void should_accept_a_command_carrying_a_pref_label_lg1() {
        assertDoesNotThrow(() -> command("Enquête"));
    }

    private static OperationCommand command(String prefLabelLg1) {
        return new OperationCommand(prefLabelLg1, "Survey", null, null, "s1001", 2024, null, null);
    }
}
