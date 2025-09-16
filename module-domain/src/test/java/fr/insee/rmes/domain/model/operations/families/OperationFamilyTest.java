package fr.insee.rmes.domain.model.operations.families;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationFamilyTest {

    @Test
    void fromJson_withAllFields() {
        JSONObject json = new JSONObject()
                .put("id", "fam001")
                .put("prefLabelLg1", "Economic Indicators")
                .put("prefLabelLg2", "Indicateurs économiques")
                .put("abstractLg1", "Family of economic indicators")
                .put("abstractLg2", "Famille des indicateurs économiques")
                .put("validationState", "VALIDATED")
                .put("created", "2023-01-01T00:00:00")
                .put("modified", "2023-06-01T12:00:00");

        OperationFamily family = OperationFamily.fromJson(json);

        assertEquals("fam001", family.id());
        assertEquals("Economic Indicators", family.prefLabelLg1());
        assertEquals("Indicateurs économiques", family.prefLabelLg2());
        assertEquals("Family of economic indicators", family.abstractLg1());
        assertEquals("Famille des indicateurs économiques", family.abstractLg2());
        assertEquals("VALIDATED", family.validationState());
        assertEquals("2023-01-01T00:00:00", family.created());
        assertEquals("2023-06-01T12:00:00", family.modified());
        assertTrue(family.series().isEmpty());
        assertTrue(family.subjects().isEmpty());
    }

    @Test
    void fromJson_withMissingFields() {
        JSONObject json = new JSONObject()
                .put("id", "fam002");

        OperationFamily family = OperationFamily.fromJson(json);

        assertEquals("fam002", family.id());
        assertNull(family.prefLabelLg1());
        assertNull(family.prefLabelLg2());
        assertNull(family.abstractLg1());
        assertNull(family.abstractLg2());
        assertNull(family.validationState());
        assertNull(family.created());
        assertNull(family.modified());
        assertTrue(family.series().isEmpty());
        assertTrue(family.subjects().isEmpty());
    }

    @Test
    void fromJson_withEmptyJson() {
        JSONObject json = new JSONObject();

        OperationFamily family = OperationFamily.fromJson(json);

        assertNull(family.id());
        assertNull(family.prefLabelLg1());
        assertNull(family.prefLabelLg2());
        assertNull(family.abstractLg1());
        assertNull(family.abstractLg2());
        assertNull(family.validationState());
        assertNull(family.created());
        assertNull(family.modified());
        assertTrue(family.series().isEmpty());
        assertTrue(family.subjects().isEmpty());
    }

    @Test
    void withSeries_replacesSeriesList() {
        OperationFamily original = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        List<OperationFamilySeries> newSeries = List.of(
                new OperationFamilySeries("s1", "Series 1", "Série 1"),
                new OperationFamilySeries("s2", "Series 2", "Série 2")
        );

        OperationFamily withSeries = original.withSeries(newSeries);

        assertEquals(original.id(), withSeries.id());
        assertEquals(original.prefLabelLg1(), withSeries.prefLabelLg1());
        assertEquals(original.prefLabelLg2(), withSeries.prefLabelLg2());
        assertEquals(original.abstractLg1(), withSeries.abstractLg1());
        assertEquals(original.abstractLg2(), withSeries.abstractLg2());
        assertEquals(original.validationState(), withSeries.validationState());
        assertEquals(original.created(), withSeries.created());
        assertEquals(original.modified(), withSeries.modified());
        assertEquals(newSeries, withSeries.series());
        assertTrue(withSeries.subjects().isEmpty());
    }

    @Test
    void withSubject_replacesSubjectsList() {
        OperationFamily original = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        List<OperationFamilySubject> newSubjects = List.of(
                new OperationFamilySubject("sub1", "Subject 1", "Sujet 1"),
                new OperationFamilySubject("sub2", "Subject 2", "Sujet 2")
        );

        OperationFamily withSubjects = original.withSubject(newSubjects);

        assertEquals(original.id(), withSubjects.id());
        assertEquals(original.prefLabelLg1(), withSubjects.prefLabelLg1());
        assertEquals(original.prefLabelLg2(), withSubjects.prefLabelLg2());
        assertEquals(original.abstractLg1(), withSubjects.abstractLg1());
        assertEquals(original.abstractLg2(), withSubjects.abstractLg2());
        assertEquals(original.validationState(), withSubjects.validationState());
        assertEquals(original.created(), withSubjects.created());
        assertEquals(original.modified(), withSubjects.modified());
        assertEquals(original.series(), withSubjects.series());
        assertEquals(newSubjects, withSubjects.subjects());
    }

    @Test
    void withSeries_maintainsImmutability() {
        OperationFamily original = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        List<OperationFamilySeries> newSeries = List.of(
                new OperationFamilySeries("s1", "Series 1", "Série 1")
        );

        OperationFamily withSeries = original.withSeries(newSeries);

        assertNotSame(original, withSeries);
        assertTrue(original.series().isEmpty());
        assertEquals(1, withSeries.series().size());
    }

    @Test
    void withSubject_maintainsImmutability() {
        OperationFamily original = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        List<OperationFamilySubject> newSubjects = List.of(
                new OperationFamilySubject("sub1", "Subject 1", "Sujet 1")
        );

        OperationFamily withSubjects = original.withSubject(newSubjects);

        assertNotSame(original, withSubjects);
        assertTrue(original.subjects().isEmpty());
        assertEquals(1, withSubjects.subjects().size());
    }

    @Test
    void record_equality() {
        OperationFamily family1 = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        OperationFamily family2 = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        assertEquals(family1, family2);
        assertEquals(family1.hashCode(), family2.hashCode());
    }

    @Test
    void record_inequality() {
        OperationFamily family1 = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        OperationFamily family2 = new OperationFamily(
                "fam002", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        assertNotEquals(family1, family2);
    }

    @Test
    void toString_containsAllFields() {
        OperationFamily family = new OperationFamily(
                "fam001", "Label1", "Label2", "Abstract1", "Abstract2",
                "DRAFT", "2023-01-01", "2023-06-01",
                Collections.emptyList(), Collections.emptyList()
        );

        String toString = family.toString();

        assertTrue(toString.contains("fam001"));
        assertTrue(toString.contains("Label1"));
        assertTrue(toString.contains("Label2"));
        assertTrue(toString.contains("Abstract1"));
        assertTrue(toString.contains("Abstract2"));
        assertTrue(toString.contains("DRAFT"));
        assertTrue(toString.contains("2023-01-01"));
        assertTrue(toString.contains("2023-06-01"));
    }
}