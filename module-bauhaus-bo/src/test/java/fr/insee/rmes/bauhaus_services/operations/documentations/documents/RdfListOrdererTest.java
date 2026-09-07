package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.json.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reconstitue l'ordre des documents d'une rubrique à partir des cellules
 * rdf:List exposées par la requête SPARQL (champs listCell/listNext).
 */
class RdfListOrdererTest {

    private static final String NIL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";

    @Test
    @DisplayName("Une liste de trois cellules est restituée dans l'ordre rdf:rest")
    void shouldOrderThreeCellsAlongRdfRest() {
        JSONArray rows = new JSONArray();
        rows.put(row("710", "cellB", "cellC"));
        rows.put(row("711", "cellC", NIL));
        rows.put(row("709", "cellA", "cellB"));

        JSONArray ordered = RdfListOrderer.orderByList(rows, "listCell", "listNext");

        assertThat(idsOf(ordered)).containsExactly("709", "710", "711");
    }

    @Test
    @DisplayName("Les champs techniques listCell/listNext sont supprimés du résultat")
    void shouldStripTechnicalFields() {
        JSONArray rows = new JSONArray();
        rows.put(row("709", "cellA", NIL));

        JSONArray ordered = RdfListOrderer.orderByList(rows, "listCell", "listNext");

        assertThat(ordered.length()).isOne();
        JSONObject only = ordered.getJSONObject(0);
        assertThat(only.has("listCell")).isFalse();
        assertThat(only.has("listNext")).isFalse();
        assertThat(only.getString("id")).isEqualTo("709");
    }

    @Test
    @DisplayName("Sans champs de liste (lecture héritée), l'array est renvoyé tel quel")
    void shouldPassThroughWhenCellsMissing() {
        JSONArray rows = new JSONArray();
        rows.put(new JSONObject().put("id", "A"));
        rows.put(new JSONObject().put("id", "B"));

        JSONArray ordered = RdfListOrderer.orderByList(rows, "listCell", "listNext");

        assertThat(idsOf(ordered)).containsExactly("A", "B");
    }

    @Test
    @DisplayName("Un tableau vide reste vide")
    void shouldHandleEmptyInput() {
        JSONArray ordered = RdfListOrderer.orderByList(new JSONArray(), "listCell", "listNext");
        assertThat(ordered.length()).isZero();
    }

    @Test
    @DisplayName("Une liste à un seul élément reste ordonnée (head=cellA, rest=nil)")
    void shouldOrderSingleton() {
        JSONArray rows = new JSONArray();
        rows.put(row("999", "cellA", NIL));

        JSONArray ordered = RdfListOrderer.orderByList(rows, "listCell", "listNext");

        assertThat(idsOf(ordered)).containsExactly("999");
    }

    private static JSONObject row(String id, String listCell, String listNext) {
        return new JSONObject()
                .put("id", id)
                .put("listCell", listCell)
                .put("listNext", listNext);
    }

    private static List<String> idsOf(JSONArray arr) {
        return JSONUtils.stream(arr)
                .map(row -> row.getString("id"))
                .toList();
    }
}
