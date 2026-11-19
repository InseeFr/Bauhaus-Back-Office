package fr.insee.rmes.modules.operations.msd.infrastructure.graphdb;

import static org.junit.jupiter.api.Assertions.*;

import fr.insee.rmes.modules.operations.msd.domain.model.DocumentationAttribute;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class DocumentationAttributeJsonMapperTest {

    @Test
    void from_json_reads_all_fields() {
        // Given
        JSONObject json = new JSONObject();
        json.put("rangeType", "STRING");
        json.put("masLabelLg1", "Label FR");
        json.put("masLabelLg2", "Label EN");
        json.put("id", "attr-123");
        json.put("maxOccurs", "1");
        json.put("isPresentational", true);
        json.put("sansObject", false);
        json.put("subPropertyOf", "http://purl.org/dc/terms/modified");

        // When
        DocumentationAttribute result = DocumentationAttributeJsonMapper.fromJson(json);

        // Then
        assertEquals("STRING", result.rangeType());
        assertEquals("Label FR", result.masLabelLg1());
        assertEquals("Label EN", result.masLabelLg2());
        assertEquals("attr-123", result.id());
        assertEquals("1", result.maxOccurs());
        assertTrue(result.isPresentational());
        assertFalse(result.sansObject());
        assertEquals("http://purl.org/dc/terms/modified", result.subPropertyOf());
    }

    @Test
    void from_json_leaves_missing_sub_property_of_null() {
        // Given
        JSONObject json = new JSONObject();

        // When
        DocumentationAttribute result = DocumentationAttributeJsonMapper.fromJson(json);

        // Then
        assertNull(result.subPropertyOf());
    }

    @Test
    void from_json_leaves_missing_string_fields_null() {
        // Given
        JSONObject json = new JSONObject();

        // When
        DocumentationAttribute result = DocumentationAttributeJsonMapper.fromJson(json);

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
    void from_json_keeps_empty_strings() {
        // Given
        JSONObject json = new JSONObject();
        json.put("rangeType", "");
        json.put("masLabelLg1", "");
        json.put("masLabelLg2", "");
        json.put("id", "");
        json.put("maxOccurs", "");

        // When
        DocumentationAttribute result = DocumentationAttributeJsonMapper.fromJson(json);

        // Then
        assertEquals("", result.rangeType());
        assertEquals("", result.masLabelLg1());
        assertEquals("", result.masLabelLg2());
        assertEquals("", result.id());
        assertEquals("", result.maxOccurs());
    }

    @Test
    void from_json_reads_boolean_is_presentational() {
        // Given - test with boolean true
        JSONObject json1 = new JSONObject();
        json1.put("isPresentational", true);

        // When
        DocumentationAttribute result1 = DocumentationAttributeJsonMapper.fromJson(json1);

        // Then
        assertTrue(result1.isPresentational());

        // Given - test with boolean false
        JSONObject json2 = new JSONObject();
        json2.put("isPresentational", false);

        // When
        DocumentationAttribute result2 = DocumentationAttributeJsonMapper.fromJson(json2);

        // Then
        assertFalse(result2.isPresentational());
    }

    @Test
    void from_json_parses_string_is_presentational() {
        // Given - test with string "true"
        JSONObject json1 = new JSONObject();
        json1.put("isPresentational", "true");

        // When
        DocumentationAttribute result1 = DocumentationAttributeJsonMapper.fromJson(json1);

        // Then
        assertTrue(result1.isPresentational());

        // Given - test with string "false"
        JSONObject json2 = new JSONObject();
        json2.put("isPresentational", "false");

        // When
        DocumentationAttribute result2 = DocumentationAttributeJsonMapper.fromJson(json2);

        // Then
        assertFalse(result2.isPresentational());

        // Given - test with invalid string
        JSONObject json3 = new JSONObject();
        json3.put("isPresentational", "invalid");

        // When
        DocumentationAttribute result3 = DocumentationAttributeJsonMapper.fromJson(json3);

        // Then
        assertFalse(result3.isPresentational());
    }

    @Test
    void from_json_reads_boolean_sans_object() {
        // Given - test with boolean true
        JSONObject json1 = new JSONObject();
        json1.put("sansObject", true);

        // When
        DocumentationAttribute result1 = DocumentationAttributeJsonMapper.fromJson(json1);

        // Then
        assertTrue(result1.sansObject());

        // Given - test with boolean false
        JSONObject json2 = new JSONObject();
        json2.put("sansObject", false);

        // When
        DocumentationAttribute result2 = DocumentationAttributeJsonMapper.fromJson(json2);

        // Then
        assertFalse(result2.sansObject());
    }
}
