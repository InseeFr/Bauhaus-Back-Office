package fr.insee.rmes.domain.model.operations.families;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationFamilySubjectTest {

    @Test
    void fromJSON_withAllFields() {
        JSONObject json = new JSONObject()
                .put("id", "subject001")
                .put("labelLg1", "Economy")
                .put("labelLg2", "Économie");

        OperationFamilySubject subject = OperationFamilySubject.fromJSON(json);

        assertEquals("subject001", subject.id());
        assertEquals("Economy", subject.labelLg1());
        assertEquals("Économie", subject.labelLg2());
    }

    @Test
    void fromJSON_withMissingFields() {
        JSONObject json = new JSONObject()
                .put("id", "subject002");

        OperationFamilySubject subject = OperationFamilySubject.fromJSON(json);

        assertEquals("subject002", subject.id());
        assertNull(subject.labelLg1());
        assertNull(subject.labelLg2());
    }

    @Test
    void fromJSON_withEmptyJson() {
        JSONObject json = new JSONObject();

        OperationFamilySubject subject = OperationFamilySubject.fromJSON(json);

        assertNull(subject.id());
        assertNull(subject.labelLg1());
        assertNull(subject.labelLg2());
    }

    @Test
    void fromJSON_withPartialFields() {
        JSONObject json = new JSONObject()
                .put("id", "subject003")
                .put("labelLg1", "Demographics");

        OperationFamilySubject subject = OperationFamilySubject.fromJSON(json);

        assertEquals("subject003", subject.id());
        assertEquals("Demographics", subject.labelLg1());
        assertNull(subject.labelLg2());
    }

    @Test
    void record_equality() {
        OperationFamilySubject subject1 = new OperationFamilySubject("sub1", "Label1", "Label2");
        OperationFamilySubject subject2 = new OperationFamilySubject("sub1", "Label1", "Label2");

        assertEquals(subject1, subject2);
        assertEquals(subject1.hashCode(), subject2.hashCode());
    }

    @Test
    void record_inequality() {
        OperationFamilySubject subject1 = new OperationFamilySubject("sub1", "Label1", "Label2");
        OperationFamilySubject subject2 = new OperationFamilySubject("sub2", "Label1", "Label2");

        assertNotEquals(subject1, subject2);
    }

    @Test
    void toString_containsAllFields() {
        OperationFamilySubject subject = new OperationFamilySubject("sub1", "Label1", "Label2");

        String toString = subject.toString();

        assertTrue(toString.contains("sub1"));
        assertTrue(toString.contains("Label1"));
        assertTrue(toString.contains("Label2"));
    }

    @Test
    void constructor_withNullValues() {
        OperationFamilySubject subject = new OperationFamilySubject(null, null, null);

        assertNull(subject.id());
        assertNull(subject.labelLg1());
        assertNull(subject.labelLg2());
    }

    @Test
    void record_accessors() {
        OperationFamilySubject subject = new OperationFamilySubject("test-id", "French Label", "English Label");

        assertEquals("test-id", subject.id());
        assertEquals("French Label", subject.labelLg1());
        assertEquals("English Label", subject.labelLg2());
    }
}