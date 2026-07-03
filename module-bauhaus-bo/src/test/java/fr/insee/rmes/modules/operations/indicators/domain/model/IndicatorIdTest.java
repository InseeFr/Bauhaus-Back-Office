package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorIdException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndicatorIdTest {

    @Test
    void should_throw_exception_if_value_is_null() {
        var exception = assertThrows(InvalidIndicatorIdException.class, () -> new IndicatorId(null));
        assertEquals("The identifier is null", exception.getMessage());
    }

    @Test
    void should_throw_exception_if_value_is_empty() {
        var exception = assertThrows(InvalidIndicatorIdException.class, () -> new IndicatorId(""));
        assertEquals("The identifier is empty", exception.getMessage());
    }

    @Test
    void should_create_indicator_id() {
        IndicatorId id = assertDoesNotThrow(() -> new IndicatorId("p1651"));
        assertEquals("p1651", id.value());
    }
}
