package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RdfListOrderer {

    private static final String RDF_NIL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";

    private RdfListOrderer() {}

    /**
     * Réordonne les lignes d'un JSONArray selon le chaînage rdf:List exposé
     * par les colonnes {@code cellField} (rdf:List node courant) et
     * {@code nextField} (rdf:List node suivant). Les lignes sans cellule sont
     * renvoyées telles quelles. Les champs techniques sont retirés du résultat.
     */
    static JSONArray orderByList(JSONArray rows, String cellField, String nextField) {
        if (rows.isEmpty() || !rows.getJSONObject(0).has(cellField)) {
            return rows;
        }
        Map<String, JSONObject> byCell = new HashMap<>();
        Set<String> referencedAsNext = new HashSet<>();
        Set<String> insertionOrder = new LinkedHashSet<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            String cell = row.getString(cellField);
            byCell.put(cell, row);
            insertionOrder.add(cell);
            String next = row.optString(nextField, "");
            if (!next.isEmpty() && !RDF_NIL.equals(next)) {
                referencedAsNext.add(next);
            }
        }
        List<JSONObject> result = new ArrayList<>(rows.length());
        Set<String> visited = new HashSet<>();
        for (String start : insertionOrder) {
            if (referencedAsNext.contains(start)) {
                continue;
            }
            walk(start, byCell, cellField, nextField, visited, result);
        }
        for (String cell : insertionOrder) {
            walk(cell, byCell, cellField, nextField, visited, result);
        }
        return new JSONArray(result);
    }

    private static void walk(String cell, Map<String, JSONObject> byCell, String cellField,
                             String nextField, Set<String> visited, List<JSONObject> out) {
        String current = cell;
        while (current != null && !RDF_NIL.equals(current) && visited.add(current)) {
            JSONObject row = byCell.get(current);
            if (row == null) return;
            row.remove(cellField);
            String next = row.optString(nextField, null);
            row.remove(nextField);
            out.add(row);
            current = next;
        }
    }
}
