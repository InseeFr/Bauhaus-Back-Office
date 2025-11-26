package fr.insee.rmes.modules.commons.configuration.swagger.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LabelUrlTest {
    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        LabelUrl labelUrl = new LabelUrl();
        List<Boolean> actual = List.of(labelUrl.label == null,
                labelUrl.url==null);
        List<Boolean> expected = List.of(true, true);
        assertEquals(expected, actual);
    }

}