package fr.insee.rmes.model.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class IndicatorTest {

    @Test
    void shouldTestEqualsForDifferentObjects() {

        Indicator indicator = Indicator.of("id");

        List<Object> objects = new ArrayList<>();
        objects.add(indicator);
        objects.add(Indicator.of("id"));
        objects.add(Indicator.of("idExample"));
        objects.add(null);
        objects.add("This is an example of string.");
        objects.add(2025);

        List<Boolean> result = new ArrayList<>();

        objects.forEach(object -> result.add(indicator.equals(object)));

        assertEquals(List.of(true, true, false, false, false, false), result);
    }
}
