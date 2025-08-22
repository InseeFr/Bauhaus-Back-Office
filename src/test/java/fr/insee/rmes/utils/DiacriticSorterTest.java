package fr.insee.rmes.utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiacriticSorterTest {

    @Test
    void testSort_shouldProcessSuccessfulllyNullKeys() throws RmesException {
        String jsonArray = """
                [
                {"id":"2", "label":"A"},
                {"id":"1"},
                {"id":"3", "label":"B"}
                ]
                """;
        assertThat(DiacriticSorter.sort(new JSONArray(jsonArray), ExampleRecord[].class, ExampleRecord::label))
                .isEqualTo(List.of(new ExampleRecord("1", null), new ExampleRecord("2", "A"), new ExampleRecord("3", "B")));
    }

    record ExampleRecord(String id, String label) {
    }

}