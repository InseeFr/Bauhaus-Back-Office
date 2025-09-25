package fr.insee.rmes.domain.model.operations;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentationAttributeTest {

    @Test
    void testFromJson_withAllFields() {
        // Given
        JSONObject json = new JSONObject();
        json.put("rangeType", "STRING");
        json.put("masLabelLg1", "Label FR");
        json.put("masLabelLg2", "Label EN");
        json.put("id", "attr-123");
        json.put("maxOccurs", "1");
        json.put("isPresentational", true);
        json.put("sansObject", false);

        // When
        DocumentationAttribute result = DocumentationAttribute.fromJson(json);

        // Then
        assertEquals("STRING", result.rangeType());
        assertEquals("Label FR", result.masLabelLg1());
        assertEquals("Label EN", result.masLabelLg2());
        assertEquals("attr-123", result.id());
        assertEquals("1", result.maxOccurs());
        assertTrue(result.isPresentational());
        assertFalse(result.sansObject());
    }

    @Test
    void testFromJson_withNullFields() {
        // Given
        JSONObject json = new JSONObject();

        // When
        DocumentationAttribute result = DocumentationAttribute.fromJson(json);

        // Then
        assertNull(result.rangeType());
        assertNull(result.masLabelLg1());
        assertNull(result.masLabelLg2());
        assertNull(result.id());
        assertNull(result.maxOccurs());
        assertFalse(result.isPresentational()); // toNullableBoolean returns false for null
        assertFalse(result.sansObject()); // optBooleanObject returns null for missing fields
    }

    @Test
    void testFromJson_withEmptyStrings() {
        // Given
        JSONObject json = new JSONObject();
        json.put("rangeType", "");
        json.put("masLabelLg1", "");
        json.put("masLabelLg2", "");
        json.put("id", "");
        json.put("maxOccurs", "");

        // When
        DocumentationAttribute result = DocumentationAttribute.fromJson(json);

        // Then
        assertEquals("", result.rangeType());
        assertEquals("", result.masLabelLg1());
        assertEquals("", result.masLabelLg2());
        assertEquals("", result.id());
        assertEquals("", result.maxOccurs());
    }

    @Test
    void testFromJson_isPresentationalBooleanTypes() {
        // Given - test with boolean true
        JSONObject json1 = new JSONObject();
        json1.put("isPresentational", true);

        // When
        DocumentationAttribute result1 = DocumentationAttribute.fromJson(json1);

        // Then
        assertTrue(result1.isPresentational());

        // Given - test with boolean false
        JSONObject json2 = new JSONObject();
        json2.put("isPresentational", false);

        // When
        DocumentationAttribute result2 = DocumentationAttribute.fromJson(json2);

        // Then
        assertFalse(result2.isPresentational());
    }

    @Test
    void testFromJson_isPresentationalStringTypes() {
        // Given - test with string "true"
        JSONObject json1 = new JSONObject();
        json1.put("isPresentational", "true");

        // When
        DocumentationAttribute result1 = DocumentationAttribute.fromJson(json1);

        // Then
        assertTrue(result1.isPresentational());

        // Given - test with string "false"
        JSONObject json2 = new JSONObject();
        json2.put("isPresentational", "false");

        // When
        DocumentationAttribute result2 = DocumentationAttribute.fromJson(json2);

        // Then
        assertFalse(result2.isPresentational());

        // Given - test with invalid string
        JSONObject json3 = new JSONObject();
        json3.put("isPresentational", "invalid");

        // When
        DocumentationAttribute result3 = DocumentationAttribute.fromJson(json3);

        // Then
        assertFalse(result3.isPresentational());
    }

    @Test
    void testFromJson_sansObjectBooleanHandling() {
        // Given - test with boolean true
        JSONObject json1 = new JSONObject();
        json1.put("sansObject", true);

        // When
        DocumentationAttribute result1 = DocumentationAttribute.fromJson(json1);

        // Then
        assertTrue(result1.sansObject());

        // Given - test with boolean false
        JSONObject json2 = new JSONObject();
        json2.put("sansObject", false);

        // When
        DocumentationAttribute result2 = DocumentationAttribute.fromJson(json2);

        // Then
        assertFalse(result2.sansObject());
    }

    @Test
    void testRecordEquality() {
        // Given
        DocumentationAttribute attr1 = new DocumentationAttribute(
            "STRING", "Label FR", "Label EN", "attr-123", "1", true, false, ""
        );
        DocumentationAttribute attr2 = new DocumentationAttribute(
            "STRING", "Label FR", "Label EN", "attr-123", "1", true, false, ""
        );
        DocumentationAttribute attr3 = new DocumentationAttribute(
            "INTEGER", "Label FR", "Label EN", "attr-123", "1", true, false, ""
        );

        // Then
        assertEquals(attr1, attr2);
        assertNotEquals(attr1, attr3);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertNotEquals(attr1.hashCode(), attr3.hashCode());
    }

    @Test
    void testRecordToString() {
        // Given
        DocumentationAttribute attr = new DocumentationAttribute(
            "STRING", "Label FR", "Label EN", "attr-123", "1", true, false, ""
        );

        // When
        String toString = attr.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("DocumentationAttribute"));
        assertTrue(toString.contains("STRING"));
        assertTrue(toString.contains("Label FR"));
        assertTrue(toString.contains("attr-123"));
    }
}