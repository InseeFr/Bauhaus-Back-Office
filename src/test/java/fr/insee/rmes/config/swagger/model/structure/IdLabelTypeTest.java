package fr.insee.rmes.config.swagger.model.structure;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IdLabelTypeTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        IdLabelType idLabelType = new IdLabelType();
        List<Boolean> actual = List.of(idLabelType.id == null,
                idLabelType.label == null,
                idLabelType.type == null
        );
        List<Boolean> expected = List.of(true, true, true);
        assertEquals(expected, actual);
    }
    
}