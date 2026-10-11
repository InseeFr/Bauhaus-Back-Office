package fr.insee.rmes.bauhaus_services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Le jeu de lignes que concepts, séries, opérations et indicateurs trient tous de la même façon :
 * tri par libellé, accents compris, et fusion des altLabels d'un même id.
 */
public final class SortedLabelRows {

    private SortedLabelRows() {}

    /** Cinq lignes, dont deux pour le même id, et des libellés accentués à trier. */
    public static JSONArray rowsWithDuplicatesAndDiacritics() {
        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
        return array;
    }

    /** Attendu pour {@link #rowsWithDuplicatesAndDiacritics()}. */
    public static <T> void assertSortedByLabelWithMergedAltLabels(
            List<T> items, Function<T, String> id, Function<T, String> label, Function<T, String> altLabel) {
        assertEquals(4, items.size());

        assertEquals("3", id.apply(items.getFirst()));
        assertEquals("alabel 1", label.apply(items.get(0)));
        assertEquals("alatLabel1", altLabel.apply(items.get(0)));

        assertEquals("2", id.apply(items.get(1)));
        assertEquals("elabel 1", label.apply(items.get(1)));
        assertEquals("elatLabel1", altLabel.apply(items.get(1)));

        assertEquals("4", id.apply(items.get(2)));
        assertEquals("élabel 1", label.apply(items.get(2)));
        assertEquals("élatLabel1", altLabel.apply(items.get(2)));

        assertEquals("1", id.apply(items.get(3)));
        assertEquals("label 1", label.apply(items.get(3)));
        assertEquals("latLabel1 || latLabel2", altLabel.apply(items.get(3)));
    }
}
