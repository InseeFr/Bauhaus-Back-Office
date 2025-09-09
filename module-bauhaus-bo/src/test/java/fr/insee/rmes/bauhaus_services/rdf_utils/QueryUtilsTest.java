package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QueryUtilsTest {

    @Test
    void shouldReturnCorrectEmptyGroupConcat() {

        String firstCase="example";
        String secondCase= "[{\"altLabel\":\"\"}]";

        String firstCaseActual = QueryUtils.correctEmptyGroupConcat(firstCase);
        String secondCaseActual= QueryUtils.correctEmptyGroupConcat(secondCase);

        boolean firstCaseExpected =firstCaseActual.equals(firstCase);
        boolean secondCaseExpected =secondCaseActual.equals("[]");

        Assertions.assertTrue(firstCaseExpected && secondCaseExpected);
    }

    @Test
    void shouldTransformRdfTypeInString() {

        JSONObject firstJsonObject = new JSONObject();
        firstJsonObject.put(Constants.TYPE_OF_OBJECT,"urn:example:example");

        JSONObject secondJsonObject = new JSONObject();
        secondJsonObject.put(Constants.ID,"idExample");

        JSONArray jsonArrayExample = new JSONArray();
        jsonArrayExample.put(firstJsonObject).put(secondJsonObject);

        JSONArray jsonArrayActual = QueryUtils.transformRdfTypeInString(jsonArrayExample);

        JSONObject firstJsonObjectExpected = new JSONObject();
        firstJsonObjectExpected .put("type","undefined");

        JSONArray jsonArrayExpected = new JSONArray();
        jsonArrayExpected .put(firstJsonObject).put(secondJsonObject);

        Assertions.assertEquals(jsonArrayExpected.toString(), jsonArrayActual.toString());

    }

}