package fr.insee.rmes.domain.model.operations.families;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartialOperationFamilyTest {

    @Test
    void constructor_withValidValues() {
        PartialOperationFamily family = new PartialOperationFamily("fam001", "Economic Indicators");

        assertEquals("fam001", family.id());
        assertEquals("Economic Indicators", family.label());
    }

    @Test
    void constructor_withNullValues() {
        PartialOperationFamily family = new PartialOperationFamily(null, null);

        assertNull(family.id());
        assertNull(family.label());
    }

    @Test
    void constructor_withMixedNullValues() {
        PartialOperationFamily family1 = new PartialOperationFamily("fam001", null);
        PartialOperationFamily family2 = new PartialOperationFamily(null, "Label");

        assertEquals("fam001", family1.id());
        assertNull(family1.label());
        assertNull(family2.id());
        assertEquals("Label", family2.label());
    }

    @Test
    void record_equality() {
        PartialOperationFamily family1 = new PartialOperationFamily("fam001", "Label");
        PartialOperationFamily family2 = new PartialOperationFamily("fam001", "Label");

        assertEquals(family1, family2);
        assertEquals(family1.hashCode(), family2.hashCode());
    }

    @Test
    void record_inequality() {
        PartialOperationFamily family1 = new PartialOperationFamily("fam001", "Label");
        PartialOperationFamily family2 = new PartialOperationFamily("fam002", "Label");
        PartialOperationFamily family3 = new PartialOperationFamily("fam001", "Different Label");

        assertNotEquals(family1, family2);
        assertNotEquals(family1, family3);
    }

    @Test
    void toString_containsAllFields() {
        PartialOperationFamily family = new PartialOperationFamily("fam001", "Economic Indicators");

        String toString = family.toString();

        assertTrue(toString.contains("fam001"));
        assertTrue(toString.contains("Economic Indicators"));
    }

    @Test
    void toString_withNullValues() {
        PartialOperationFamily family = new PartialOperationFamily(null, null);

        String toString = family.toString();

        assertTrue(toString.contains("null"));
    }

    @Test
    void record_accessors() {
        PartialOperationFamily family = new PartialOperationFamily("test-id", "Test Label");

        assertEquals("test-id", family.id());
        assertEquals("Test Label", family.label());
    }

    @Test
    void record_immutability() {
        PartialOperationFamily family = new PartialOperationFamily("fam001", "Label");
        
        // Records are immutable, so we cannot modify values after creation
        // This test ensures the getters return the same values
        assertEquals("fam001", family.id());
        assertEquals("Label", family.label());
        
        // Creating a new instance with different values should not affect the original
        PartialOperationFamily anotherFamily = new PartialOperationFamily("fam002", "Another Label");
        assertEquals("fam001", family.id());
        assertEquals("Label", family.label());
        assertEquals("fam002", anotherFamily.id());
        assertEquals("Another Label", anotherFamily.label());
    }
}