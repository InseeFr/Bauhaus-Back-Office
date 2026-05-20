package fr.insee.rmes.bauhaus_services.code_list;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeListKindTest {

    @Test
    void partialIsPartial() {
        assertTrue(CodeListKind.PARTIAL.isPartial());
    }

    @Test
    void fullIsNotPartial() {
        assertFalse(CodeListKind.FULL.isPartial());
    }
}
