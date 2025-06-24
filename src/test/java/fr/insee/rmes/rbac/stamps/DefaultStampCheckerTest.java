package fr.insee.rmes.rbac.stamps;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class DefaultStampCheckerTest {

    @ParameterizedTest
    @ValueSource(strings = { "a", "b","2025","32","example" })
    void shouldReturnEmptyListWhenGetStamps(String id) {
        DefaultStampChecker defaultStampChecker = new DefaultStampChecker();
        String actual = defaultStampChecker.getStamps(id).toString();
        assertEquals("[]",actual);
    }
}