package fr.insee.rmes.modules.operations.msd.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DocumentationAttributeTest {

    @Test
    void records_with_same_fields_are_equal() {
        // Given
        DocumentationAttribute attr1 = new DocumentationAttribute(
                "STRING", "Label FR", "Label EN", "attr-123", "1", true, false, "", null, null, null);
        DocumentationAttribute attr2 = new DocumentationAttribute(
                "STRING", "Label FR", "Label EN", "attr-123", "1", true, false, "", null, null, null);
        DocumentationAttribute attr3 = new DocumentationAttribute(
                "INTEGER", "Label FR", "Label EN", "attr-123", "1", true, false, "", null, null, null);

        // Then
        assertEquals(attr1, attr2);
        assertNotEquals(attr1, attr3);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertNotEquals(attr1.hashCode(), attr3.hashCode());
    }

    @Test
    void to_string_contains_record_name_and_field_values() {
        // Given
        DocumentationAttribute attr = new DocumentationAttribute(
                "STRING", "Label FR", "Label EN", "attr-123", "1", true, false, "", null, null, null);

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
