package fr.insee.rmes.infrastructure.utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeserializerTest {


    // Simple test class for deserialization
    public static class TestObject {
        private String name;
        private int age;
        private boolean active;

        public TestObject() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    @Test
    void shouldDeserializeJsonString() throws RmesException {
        String json = "{\"name\":\"John\",\"age\":30,\"active\":true}";
        
        TestObject result = Deserializer.deserializeJsonString(json, TestObject.class);
        
        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
        assertTrue(result.isActive());
    }

    @Test
    void shouldDeserializeJsonStringWithMissingProperties() throws RmesException {
        String json = "{\"name\":\"Jane\"}";
        
        TestObject result = Deserializer.deserializeJsonString(json, TestObject.class);
        
        assertNotNull(result);
        assertEquals("Jane", result.getName());
        assertEquals(0, result.getAge()); // Default value
        assertFalse(result.isActive()); // Default value
    }

    @Test
    void shouldIgnoreUnknownProperties() throws RmesException {
        String json = "{\"name\":\"Bob\",\"age\":25,\"unknownProperty\":\"should be ignored\"}";
        
        TestObject result = Deserializer.deserializeJsonString(json, TestObject.class);
        
        assertNotNull(result);
        assertEquals("Bob", result.getName());
        assertEquals(25, result.getAge());
    }

    @Test
    void shouldThrowRmesExceptionForInvalidJson() {
        String invalidJson = "{\"name\":\"John\",\"age\":}"; // Invalid JSON
        
        assertThrows(RmesException.class, () -> {
            Deserializer.deserializeJsonString(invalidJson, TestObject.class);
        });
    }

    @Test
    void shouldDeserializeEmptyJsonObject() throws RmesException {
        String json = "{}";
        
        TestObject result = Deserializer.deserializeJsonString(json, TestObject.class);
        
        assertNotNull(result);
        assertNull(result.getName());
        assertEquals(0, result.getAge());
        assertFalse(result.isActive());
    }

    @Test
    void shouldDeserializeJSONArray() throws RmesException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject().put("name", "Alice").put("age", 28));
        jsonArray.put(new JSONObject().put("name", "Bob").put("age", 32));
        
        TestObject[] result = Deserializer.deserializeJSONArray(jsonArray, TestObject[].class);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("Alice", result[0].getName());
        assertEquals(28, result[0].getAge());
        assertEquals("Bob", result[1].getName());
        assertEquals(32, result[1].getAge());
    }

    @Test
    void shouldDeserializeEmptyJSONArray() throws RmesException {
        JSONArray jsonArray = new JSONArray();
        
        TestObject[] result = Deserializer.deserializeJSONArray(jsonArray, TestObject[].class);
        
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void shouldDeserializeJSONObject() throws RmesException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Charlie");
        jsonObject.put("age", 35);
        jsonObject.put("active", true);
        
        TestObject result = Deserializer.deserializeJSONObject(jsonObject, TestObject.class);
        
        assertNotNull(result);
        assertEquals("Charlie", result.getName());
        assertEquals(35, result.getAge());
        assertTrue(result.isActive());
    }

    @Test
    void shouldDeserializeComplexJSONObject() throws RmesException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "David");
        jsonObject.put("age", 40);
        jsonObject.put("active", false);
        jsonObject.put("extraProperty", "ignored");
        
        TestObject result = Deserializer.deserializeJSONObject(jsonObject, TestObject.class);
        
        assertNotNull(result);
        assertEquals("David", result.getName());
        assertEquals(40, result.getAge());
        assertFalse(result.isActive());
    }

    @Test
    void shouldHandleNullValues() throws RmesException {
        String json = "{\"name\":null,\"age\":0,\"active\":false}";
        
        TestObject result = Deserializer.deserializeJsonString(json, TestObject.class);
        
        assertNotNull(result);
        assertNull(result.getName());
        assertEquals(0, result.getAge());
        assertFalse(result.isActive());
    }

    @Test
    void shouldDeserializeStringToString() throws RmesException {
        String json = "\"Hello World\"";
        
        String result = Deserializer.deserializeJsonString(json, String.class);
        
        assertEquals("Hello World", result);
    }

    @Test
    void shouldDeserializeNumberToInteger() throws RmesException {
        String json = "42";
        
        Integer result = Deserializer.deserializeJsonString(json, Integer.class);
        
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void shouldDeserializeBooleanToBoolean() throws RmesException {
        String json = "true";
        
        Boolean result = Deserializer.deserializeJsonString(json, Boolean.class);
        
        assertTrue(result);
    }

    @Test
    void shouldThrowRmesExceptionForTypeMismatch() {
        String json = "\"not a number\"";
        
        assertThrows(RmesException.class, () -> {
            Deserializer.deserializeJsonString(json, Integer.class);
        });
    }
}