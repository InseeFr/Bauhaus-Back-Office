package fr.insee.rmes.config.swagger.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IdLabelAltLabelSimsTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        IdLabelAltLabelSims idLabelAltLabelSims = new IdLabelAltLabelSims();
        List<Boolean> actual = List.of(idLabelAltLabelSims.id == null,
                idLabelAltLabelSims.label == null,
                idLabelAltLabelSims.altLabel==null,
                idLabelAltLabelSims.idSims==null);
        List<Boolean> expected = List.of(true, true,true,true);
        assertEquals(expected, actual);
    }

}