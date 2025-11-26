package fr.insee.rmes.modules.commons.configuration.swagger.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IdLabelAltLabelTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        IdLabelAltLabel idLabelAltLabel = new IdLabelAltLabel();
        List<Boolean> actual = List.of(idLabelAltLabel.label == null,
                idLabelAltLabel.id == null,
                idLabelAltLabel.altLabel==null);
        List<Boolean> expected = List.of(true, true,true);
        assertEquals(expected, actual);
    }

}