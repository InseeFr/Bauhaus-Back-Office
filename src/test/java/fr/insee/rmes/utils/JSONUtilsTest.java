package fr.insee.rmes.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JSONUtilsTest {

    @Test
    void testExtractFieldToArray() {
        JSONArray inputArray = new JSONArray();
        inputArray.put(new JSONObject().put("name", "Alice").put("age", 25));
        inputArray.put(new JSONObject().put("name", "Bob").put("age", 30));
        inputArray.put(new JSONObject().put("name", "Charlie").put("age", 35));

        JSONArray result = JSONUtils.extractFieldToArray(inputArray, "name");

        assertEquals(3, result.length());
        assertEquals("Alice", result.getString(0));
        assertEquals("Bob", result.getString(1));
        assertEquals("Charlie", result.getString(2));

        // Cas 2 : JSONArray vide
        JSONArray emptyArray = new JSONArray();
        result = JSONUtils.extractFieldToArray(emptyArray, "name");

        assertEquals(0, result.length());
    }

    @Test
    void testJsonArrayOfStringToString() {
        JSONArray singleElementArray = new JSONArray();
        singleElementArray.put("Alice");
        String result = JSONUtils.jsonArrayOfStringToString(singleElementArray);
        assertEquals("Alice", result);

        JSONArray multipleElementsArray = new JSONArray();
        multipleElementsArray.put("Alice");
        multipleElementsArray.put("Bob");
        multipleElementsArray.put("Charlie");
        result = JSONUtils.jsonArrayOfStringToString(multipleElementsArray);
        assertEquals("Alice - Bob - Charlie", result);
    }

    @Test
    void testIsEmpty() {
        JSONObject emptyObject = new JSONObject();
        assertTrue(JSONUtils.isEmpty(emptyObject));

        JSONObject objWithEmptyValues = new JSONObject();
        objWithEmptyValues.put("key1", "");
        objWithEmptyValues.put("key2", "");
        assertTrue(JSONUtils.isEmpty(objWithEmptyValues));

        JSONObject objWithNonEmptyValue = new JSONObject();
        objWithNonEmptyValue.put("key1", "");
        objWithNonEmptyValue.put("key2", "value");
        assertFalse(JSONUtils.isEmpty(objWithNonEmptyValue));

        JSONObject objWithNonEmptyValues = new JSONObject();
        objWithNonEmptyValues.put("key1", "value1");
        objWithNonEmptyValues.put("key2", "value2");
        assertFalse(JSONUtils.isEmpty(objWithNonEmptyValues));
    }

    @Test
    void testStreamConversion() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject().put("id", 1).put("name", "Alice"));
        jsonArray.put(new JSONObject().put("id", 2).put("name", "Bob"));
        jsonArray.put(new JSONObject().put("id", 3).put("name", "Charlie"));

        Stream<JSONObject> stream = JSONUtils.stream(jsonArray);

        List<JSONObject> result = stream.collect(Collectors.toList());

        assertEquals(3, result.size());
        assertEquals("Alice", result.get(0).getString("name"));
        assertEquals(1, result.get(0).getInt("id"));

        assertEquals("Bob", result.get(1).getString("name"));
        assertEquals(2, result.get(1).getInt("id"));

        assertEquals("Charlie", result.get(2).getString("name"));
        assertEquals(3, result.get(2).getInt("id"));
    }

    @Test
    void testJsonArrayToList() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("Alice");
        jsonArray.put("Bob");
        jsonArray.put("Charlie");

        List<String> result = JSONUtils.jsonArrayToList(jsonArray);

        // Vérifications
        assertEquals(3, result.size());
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Bob"));
        assertTrue(result.contains("Charlie"));

        assertEquals("Alice", result.get(0));
        assertEquals("Bob", result.get(1));
        assertEquals("Charlie", result.get(2));
    }
}