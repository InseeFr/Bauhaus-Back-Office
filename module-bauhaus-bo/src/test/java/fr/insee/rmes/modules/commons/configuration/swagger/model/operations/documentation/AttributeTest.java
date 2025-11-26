package fr.insee.rmes.modules.commons.configuration.swagger.model.operations.documentation;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AttributeTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        Attribute attribute = new Attribute();
        List<Boolean> actual = List.of(attribute.id == null,
                attribute.masLabelLg1 == null,
                attribute.masLabelLg2 == null,
                attribute.rangeType == null,
                attribute.isPresentational == null,
                attribute.codeList == null
                );
        List<Boolean> expected = List.of(true, true, true, true,true,true);
        assertEquals(expected, actual);
    }
}