package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.graphdb.annotations.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.*;

class PartialCodesListTest {

    @Test
    void shouldHaveCorrectClassAnnotations() {
        assertTrue(PartialCodesList.class.isAnnotationPresent(Entity.class));
        assertTrue(PartialCodesList.class.isAnnotationPresent(Graph.class));
        
        Entity entityAnnotation = PartialCodesList.class.getAnnotation(Entity.class);
        assertEquals("skos:Collection", entityAnnotation.type());
        assertEquals("", entityAnnotation.value());
        
        Graph graphAnnotation = PartialCodesList.class.getAnnotation(Graph.class);
        assertEquals("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}", 
                    graphAnnotation.value());
    }

    @Test
    void shouldHaveCorrectRecordComponents() {
        RecordComponent[] components = PartialCodesList.class.getRecordComponents();
        assertEquals(5, components.length);
        
        String[] expectedComponents = {"id", "uri", "labelLg1", "labelLg2", "range"};
        for (String expected : expectedComponents) {
            RecordComponent component = getRecordComponent(PartialCodesList.class, expected);
            assertNotNull(component, "Missing component: " + expected);
        }
    }

    @Test
    void shouldHaveCorrectIdAnnotation() throws Exception {
        RecordComponent idComponent = getRecordComponent(PartialCodesList.class, "id");
        assertNotNull(idComponent);
        
        assertTrue(idComponent.isAnnotationPresent(Predicate.class));
        Predicate predicate = idComponent.getAnnotation(Predicate.class);
        assertEquals("skos:notation", predicate.value());
        assertEquals("", predicate.namespace());
        assertTrue(predicate.optional());
    }

    @Test
    void shouldHaveCorrectUriAnnotation() throws Exception {
        RecordComponent uriComponent = getRecordComponent(PartialCodesList.class, "uri");
        assertNotNull(uriComponent);
        
        assertTrue(uriComponent.isAnnotationPresent(Statement.class));
        assertFalse(uriComponent.isAnnotationPresent(Predicate.class));
    }

    @Test
    void shouldHaveCorrectLabelAnnotations() throws Exception {
        RecordComponent labelLg1Component = getRecordComponent(PartialCodesList.class, "labelLg1");
        assertNotNull(labelLg1Component);
        
        assertTrue(labelLg1Component.isAnnotationPresent(Predicate.class));
        assertTrue(labelLg1Component.isAnnotationPresent(DefaultSortField.class));
        
        Predicate predicate = labelLg1Component.getAnnotation(Predicate.class);
        assertEquals("skos:prefLabel", predicate.value());
        assertTrue(predicate.optional());
        
        RecordComponent labelLg2Component = getRecordComponent(PartialCodesList.class, "labelLg2");
        assertNotNull(labelLg2Component);
        
        assertTrue(labelLg2Component.isAnnotationPresent(Predicate.class));
        assertFalse(labelLg2Component.isAnnotationPresent(DefaultSortField.class));
        
        Predicate predicate2 = labelLg2Component.getAnnotation(Predicate.class);
        assertEquals("skos:prefLabel", predicate2.value());
        assertTrue(predicate2.optional());
    }

    @Test
    void shouldHaveCorrectRangeAnnotation() throws Exception {
        RecordComponent rangeComponent = getRecordComponent(PartialCodesList.class, "range");
        assertNotNull(rangeComponent);
        
        assertTrue(rangeComponent.isAnnotationPresent(Predicate.class));
        Predicate predicate = rangeComponent.getAnnotation(Predicate.class);
        assertEquals("rdfs:seeAlso", predicate.value());
        assertTrue(predicate.optional());
    }

    @Test
    void shouldHaveOnlyOneDefaultSortField() {
        RecordComponent[] components = PartialCodesList.class.getRecordComponents();
        int defaultSortFieldCount = 0;
        String defaultSortFieldName = null;
        
        for (RecordComponent component : components) {
            if (component.isAnnotationPresent(DefaultSortField.class)) {
                defaultSortFieldCount++;
                defaultSortFieldName = component.getName();
            }
        }
        
        assertEquals(1, defaultSortFieldCount, "Should have exactly one @DefaultSortField");
        assertEquals("labelLg1", defaultSortFieldName, "Default sort field should be labelLg1");
    }

    @Test
    void shouldCreateRecordInstance() {
        PartialCodesList partialCodesList = new PartialCodesList(
            "PARTIAL001",
            "http://example.com/partial/PARTIAL001",
            "Partial Label FR",
            "Partial Label EN",
            "http://example.com/range"
        );
        
        assertNotNull(partialCodesList);
        assertEquals("PARTIAL001", partialCodesList.id());
        assertEquals("http://example.com/partial/PARTIAL001", partialCodesList.uri());
        assertEquals("Partial Label FR", partialCodesList.labelLg1());
        assertEquals("Partial Label EN", partialCodesList.labelLg2());
        assertEquals("http://example.com/range", partialCodesList.range());
    }

    @Test
    void shouldHandleNullValues() {
        PartialCodesList partialCodesList = new PartialCodesList(null, null, null, null, null);
        
        assertNotNull(partialCodesList);
        assertNull(partialCodesList.id());
        assertNull(partialCodesList.uri());
        assertNull(partialCodesList.labelLg1());
        assertNull(partialCodesList.labelLg2());
        assertNull(partialCodesList.range());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        PartialCodesList partialCodesList1 = new PartialCodesList("PARTIAL001", "http://example.com/test", "Label1", "Label2", "Range");
        PartialCodesList partialCodesList2 = new PartialCodesList("PARTIAL001", "http://example.com/test", "Label1", "Label2", "Range");
        PartialCodesList partialCodesList3 = new PartialCodesList("PARTIAL002", "http://example.com/test2", "Label3", "Label4", "Range2");
        
        assertEquals(partialCodesList1, partialCodesList2);
        assertNotEquals(partialCodesList1, partialCodesList3);
        assertEquals(partialCodesList1.hashCode(), partialCodesList2.hashCode());
    }

    @Test
    void shouldImplementToString() {
        PartialCodesList partialCodesList = new PartialCodesList("PARTIAL001", "http://example.com/test", "Label1", "Label2", "Range");
        String toString = partialCodesList.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("PARTIAL001"));
        assertTrue(toString.contains("http://example.com/test"));
        assertTrue(toString.contains("Label1"));
        assertTrue(toString.contains("Label2"));
        assertTrue(toString.contains("Range"));
    }

    @Test
    void shouldBeRecord() {
        assertTrue(PartialCodesList.class.isRecord());
    }

    @Test
    void shouldHaveCorrectComponentTypes() throws Exception {
        RecordComponent idComponent = getRecordComponent(PartialCodesList.class, "id");
        assertEquals(String.class, idComponent.getType());
        
        RecordComponent uriComponent = getRecordComponent(PartialCodesList.class, "uri");
        assertEquals(String.class, uriComponent.getType());
        
        RecordComponent labelLg1Component = getRecordComponent(PartialCodesList.class, "labelLg1");
        assertEquals(String.class, labelLg1Component.getType());
        
        RecordComponent labelLg2Component = getRecordComponent(PartialCodesList.class, "labelLg2");
        assertEquals(String.class, labelLg2Component.getType());
        
        RecordComponent rangeComponent = getRecordComponent(PartialCodesList.class, "range");
        assertEquals(String.class, rangeComponent.getType());
    }

    @Test
    void shouldHaveOptionalFieldsMarkedCorrectly() throws Exception {
        RecordComponent labelLg1Component = getRecordComponent(PartialCodesList.class, "labelLg1");
        Predicate labelLg1Predicate = labelLg1Component.getAnnotation(Predicate.class);
        assertNotNull(labelLg1Predicate);
        assertTrue(labelLg1Predicate.optional());
        
        RecordComponent labelLg2Component = getRecordComponent(PartialCodesList.class, "labelLg2");
        Predicate labelLg2Predicate = labelLg2Component.getAnnotation(Predicate.class);
        assertNotNull(labelLg2Predicate);
        assertTrue(labelLg2Predicate.optional());
        
        RecordComponent rangeComponent = getRecordComponent(PartialCodesList.class, "range");
        Predicate rangePredicate = rangeComponent.getAnnotation(Predicate.class);
        assertNotNull(rangePredicate);
        assertTrue(rangePredicate.optional());
    }

    @Test
    void shouldHandlePartiallyNullFields() {
        PartialCodesList partialCodesList = new PartialCodesList(
            "PARTIAL001",
            "http://example.com/partial/PARTIAL001",
            "Partial Label FR",
            null,
            null
        );
        
        assertNotNull(partialCodesList);
        assertEquals("PARTIAL001", partialCodesList.id());
        assertEquals("http://example.com/partial/PARTIAL001", partialCodesList.uri());
        assertEquals("Partial Label FR", partialCodesList.labelLg1());
        assertNull(partialCodesList.labelLg2());
        assertNull(partialCodesList.range());
    }

    private RecordComponent getRecordComponent(Class<?> recordClass, String componentName) {
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            if (component.getName().equals(componentName)) {
                return component;
            }
        }
        return null;
    }
}