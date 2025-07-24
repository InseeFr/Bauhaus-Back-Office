package fr.insee.rmes.config.swagger.model.application;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PersonsTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        Persons persons = new Persons();
        List<Boolean> actual = List.of(persons.id == null,
                persons.label==null,
                persons.stamp==null);
        List<Boolean> expected = List.of(true, true,true);
        assertEquals(expected, actual);
    }

}