package fr.insee.rmes.bauhaus_services.rdf_utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static fr.insee.rmes.bauhaus_services.Constants.VALUE;
import static org.eclipse.rdf4j.query.resultio.sparqljson.AbstractSPARQLJSONParser.BINDINGS;
import static org.eclipse.rdf4j.query.resultio.sparqljson.AbstractSPARQLJSONParser.RESULTS;

class RepositoryUtilsTest {

    public JSONObject createTestJsonObject(){
        JSONObject river = new JSONObject();
        river.put(VALUE, "Tamise").put("length","unknown");

        JSONObject monument = new JSONObject();
        monument.put(VALUE, "Tower of London").put("high","unknown");

        JSONObject description = new JSONObject();
        description.put("monument", monument).put("river",river);

        JSONArray countries = new JSONArray();
        countries.put(description);

        JSONObject world = new JSONObject();
        world.put(BINDINGS,countries);

        JSONObject jsonSparql = new JSONObject();
        jsonSparql.put(RESULTS, world);

        return jsonSparql;

    }

    @Test
    void shouldReturnResultArrayValuesFromSparqlJSON () {
        JSONObject jsonSparql = createTestJsonObject();
        JSONArray actual = RepositoryUtils.sparqlJSONToResultArrayValues(jsonSparql);
        boolean actualCorrectValue = "[{\"monument\":\"Tower of London\",\"river\":\"Tamise\"}]".equals(actual.toString());
        Assertions.assertTrue(actualCorrectValue);

    }

    @Test
    void shouldReturnResultListValuesFromSparqlJSON () {
        JSONObject jsonSparql = createTestJsonObject();
        JSONArray actual = RepositoryUtils.sparqlJSONToResultListValues(jsonSparql);
        boolean actualCorrectValue = "[\"Tower of London\",\"Tamise\"]".equals(actual.toString());
        Assertions.assertTrue(actualCorrectValue);
    }

}