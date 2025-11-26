package fr.insee.rmes.modules.commons.configuration.swagger.model.structure;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StructureByIdTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        StructureById structureById = new StructureById();
        List<Boolean> actual = List.of(structureById.labelLg1 == null,
                structureById.labelLg2 == null,
                structureById.descriptionLg1 == null,
                structureById.descriptionLg2 == null);
        List<Boolean> expected = List.of(true, true, true,true);
        assertEquals(expected, actual);
    }

}