package fr.insee.rmes.domain.logging;

import static fr.insee.rmes.domain.logging.LogSanitizer.forLog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LogSanitizerTest {

    @Test
    void shouldLeaveAnOrdinaryValueUnchanged() {
        assertEquals("fr.insee-C1234", forLog("fr.insee-C1234"));
    }

    @Test
    void shouldReplaceTheLineBreaksThatWouldForgeANewLogEntry() {
        assertEquals("id__2026-09-10 ERROR fausse entree", forLog("id\r\n2026-09-10 ERROR fausse entree"));
    }

    @Test
    void shouldReplaceControlCharactersButKeepSpaces() {
        assertEquals("a_b c", forLog("a\tb c"));
    }

    @Test
    void shouldReturnNullForANullValue() {
        assertNull(forLog(null));
    }
}
