package fr.insee.rmes.modules.commons.configuration.swagger.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IdLabelTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        IdLabel idLabel = new IdLabel();
        List<Boolean> actual = List.of(idLabel.label == null,
                idLabel.id == null);
        List<Boolean> expected = List.of(true, true);
        assertEquals(expected, actual);
    }

}