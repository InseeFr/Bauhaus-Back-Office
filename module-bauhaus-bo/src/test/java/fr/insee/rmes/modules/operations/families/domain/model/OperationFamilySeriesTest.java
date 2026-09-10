package fr.insee.rmes.modules.operations.families.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class OperationFamilySeriesTest {

    @Test
    void from_json_with_all_fields() {
        JSONObject json = new JSONObject()
                .put("id", "series001")
                .put("labelLg1", "Economic Series")
                .put("labelLg2", "Série économique");

        OperationFamilySeries series = OperationFamilySeries.fromJSON(json);

        assertEquals("series001", series.id());
        assertEquals("Economic Series", series.labelLg1());
        assertEquals("Série économique", series.labelLg2());
    }

    @Test
    void from_json_with_missing_fields() {
        JSONObject json = new JSONObject().put("id", "series002");

        OperationFamilySeries series = OperationFamilySeries.fromJSON(json);

        assertEquals("series002", series.id());
        assertNull(series.labelLg1());
        assertNull(series.labelLg2());
    }

    @Test
    void from_json_with_empty_json() {
        JSONObject json = new JSONObject();

        OperationFamilySeries series = OperationFamilySeries.fromJSON(json);

        assertNull(series.id());
        assertNull(series.labelLg1());
        assertNull(series.labelLg2());
    }

    @Test
    void from_json_with_partial_fields() {
        JSONObject json = new JSONObject().put("id", "series003").put("labelLg1", "Population Series");

        OperationFamilySeries series = OperationFamilySeries.fromJSON(json);

        assertEquals("series003", series.id());
        assertEquals("Population Series", series.labelLg1());
        assertNull(series.labelLg2());
    }

    @Test
    void record_equality() {
        OperationFamilySeries series1 = new OperationFamilySeries("s1", "Label1", "Label2");
        OperationFamilySeries series2 = new OperationFamilySeries("s1", "Label1", "Label2");

        assertEquals(series1, series2);
        assertEquals(series1.hashCode(), series2.hashCode());
    }

    @Test
    void record_inequality() {
        OperationFamilySeries series1 = new OperationFamilySeries("s1", "Label1", "Label2");
        OperationFamilySeries series2 = new OperationFamilySeries("s2", "Label1", "Label2");

        assertNotEquals(series1, series2);
    }

    @Test
    void to_string_contains_all_fields() {
        OperationFamilySeries series = new OperationFamilySeries("s1", "Label1", "Label2");

        String toString = series.toString();

        assertTrue(toString.contains("s1"));
        assertTrue(toString.contains("Label1"));
        assertTrue(toString.contains("Label2"));
    }

    @Test
    void constructor_with_null_values() {
        OperationFamilySeries series = new OperationFamilySeries(null, null, null);

        assertNull(series.id());
        assertNull(series.labelLg1());
        assertNull(series.labelLg2());
    }

    @Test
    void record_accessors() {
        OperationFamilySeries series = new OperationFamilySeries("test-id", "French Label", "English Label");

        assertEquals("test-id", series.id());
        assertEquals("French Label", series.labelLg1());
        assertEquals("English Label", series.labelLg2());
    }
}
