package fr.insee.rmes.graphdb;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class QueryUtilsTest {

    @Test
    void shouldCorrectEmptyGroupConcatForAltLabel() {
        String input = "[{\"altLabel\":\"\"}]";
        String result = QueryUtils.correctEmptyGroupConcat(input);
        assertEquals("[]", result);
    }

    @Test
    void shouldNotModifyNonEmptyGroupConcat() {
        String input = "[{\"altLabel\":\"some value\"}]";
        String result = QueryUtils.correctEmptyGroupConcat(input);
        assertEquals(input, result);
    }

    @Test
    void shouldNotModifyDifferentStructure() {
        String input = "[{\"otherField\":\"\"}]";
        String result = QueryUtils.correctEmptyGroupConcat(input);
        assertEquals(input, result);
    }

    @Test
    void shouldNotModifyEmptyString() {
        String input = "";
        String result = QueryUtils.correctEmptyGroupConcat(input);
        assertEquals(input, result);
    }

    @Test
    void shouldTransformRdfTypeInString() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("typeOfObject", "http://www.w3.org/2004/02/skos/core#Concept");
        jsonObject.put("id", "test123");
        jsonArray.put(jsonObject);

        JSONArray result = QueryUtils.transformRdfTypeInString(jsonArray);

        assertNotNull(result);
        assertEquals(1, result.length());
        JSONObject resultObject = result.getJSONObject(0);
        assertTrue(resultObject.has("type"));
        assertFalse(resultObject.has("typeOfObject"));
        assertEquals("test123", resultObject.getString("id"));
    }

    @Test
    void shouldHandleArrayWithoutTypeOfObject() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "test123");
        jsonObject.put("name", "Test Name");
        jsonArray.put(jsonObject);

        JSONArray result = QueryUtils.transformRdfTypeInString(jsonArray);

        assertNotNull(result);
        assertEquals(1, result.length());
        JSONObject resultObject = result.getJSONObject(0);
        assertFalse(resultObject.has("type"));
        assertFalse(resultObject.has("typeOfObject"));
        assertEquals("test123", resultObject.getString("id"));
        assertEquals("Test Name", resultObject.getString("name"));
    }

    @Test
    void shouldHandleEmptyArray() {
        JSONArray jsonArray = new JSONArray();

        JSONArray result = QueryUtils.transformRdfTypeInString(jsonArray);

        assertNotNull(result);
        assertEquals(0, result.length());
    }

    @Test
    void shouldHandleMultipleObjectsInArray() {
        JSONArray jsonArray = new JSONArray();

        JSONObject obj1 = new JSONObject();
        obj1.put("typeOfObject", "http://www.w3.org/2004/02/skos/core#Concept");
        obj1.put("id", "test1");

        JSONObject obj2 = new JSONObject();
        obj2.put("id", "test2");
        obj2.put("name", "No type");

        JSONObject obj3 = new JSONObject();
        obj3.put("typeOfObject", "http://www.w3.org/2004/02/skos/core#ConceptScheme");
        obj3.put("id", "test3");

        jsonArray.put(obj1);
        jsonArray.put(obj2);
        jsonArray.put(obj3);

        JSONArray result = QueryUtils.transformRdfTypeInString(jsonArray);

        assertEquals(3, result.length());

        // First object should have type
        assertTrue(result.getJSONObject(0).has("type"));
        assertFalse(result.getJSONObject(0).has("typeOfObject"));

        // Second object should remain unchanged
        assertFalse(result.getJSONObject(1).has("type"));
        assertFalse(result.getJSONObject(1).has("typeOfObject"));

        // Third object should have type
        assertTrue(result.getJSONObject(2).has("type"));
        assertFalse(result.getJSONObject(2).has("typeOfObject"));
    }
}
