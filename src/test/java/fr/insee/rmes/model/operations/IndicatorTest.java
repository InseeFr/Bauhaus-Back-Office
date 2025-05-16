package fr.insee.rmes.model.operations;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IndicatorTest {

    @Test
    void shouldTestEqualsForDifferentObjects() {

        Indicator indicator = new Indicator("id");

        List<Object> objects = new ArrayList<>();
        objects.add(indicator);
        objects.add(new Indicator("id"));
        objects.add(new Indicator("idExample"));
        objects.add(null);
        objects.add("This is an example of string.");
        objects.add(2025);

        List<Boolean> result = new ArrayList<>();

        objects.forEach(object->result.add(indicator.equals(object)));

        assertEquals(List.of(true,true,false,false,false,false),result);
    }

}