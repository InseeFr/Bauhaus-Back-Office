package fr.insee.rmes.modules.commons.configuration.swagger.model.concepts;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConceptsToValidateTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        ConceptsToValidate conceptsToValidate = new ConceptsToValidate();
        List<Boolean> actual = List.of(conceptsToValidate.id == null,
                conceptsToValidate.label == null,
                conceptsToValidate.creator == null,
                conceptsToValidate.valid == null);
        List<Boolean> expected = List.of(true, true, true, true);
        assertEquals(expected, actual);
    }
}