package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.sparql.annotations.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.*;

class PartialCodesListTest {

    @Test
    void shouldHaveCorrectClassAnnotations() {
        assertTrue(PartialCodesList.class.isAnnotationPresent(Entity.class));
        assertTrue(PartialCodesList.class.isAnnotationPresent(Graph.class));
        
        final Entity entityAnnotation = PartialCodesList.class.getAnnotation(Entity.class);
        assertEquals("skos:Collection", entityAnnotation.type());
        assertEquals("", entityAnnotation.value());
        
        final Graph graphAnnotation = PartialCodesList.class.getAnnotation(Graph.class);
        assertEquals("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}", 
                    graphAnnotation.value());
    }

    @Test
    void shouldHaveCorrectRecordComponents() {
        final RecordComponent[] components = PartialCodesList.class.getRecordComponents();
        assertEquals(5, components.length);
        
        final String[] expectedComponents = {"id", "uri", "labelLg1", "labelLg2", "range"};
        for (final String expected : expectedComponents) {
            final RecordComponent component = this.getRecordComponent(PartialCodesList.class, expected);
            assertNotNull(component, "Missing component: " + expected);
        }
    }

    @Test
    void shouldHaveCorrectIdAnnotation() throws Exception {
        final RecordComponent idComponent = this.getRecordComponent(PartialCodesList.class, "id");
        assertNotNull(idComponent);
        
        assertTrue(idComponent.isAnnotationPresent(Predicate.class));
        final Predicate predicate = idComponent.getAnnotation(Predicate.class);
        assertEquals("skos:notation", predicate.value());
        assertEquals("", predicate.namespace());
        assertFalse(predicate.optional());
    }

    @Test
    void shouldHaveCorrectUriAnnotation() throws Exception {
        final RecordComponent uriComponent = this.getRecordComponent(PartialCodesList.class, "uri");
        assertNotNull(uriComponent);
        
        assertTrue(uriComponent.isAnnotationPresent(Statement.class));
        assertFalse(uriComponent.isAnnotationPresent(Predicate.class));
    }

    @Test
    void shouldHaveCorrectLabelAnnotations() throws Exception {
        final RecordComponent labelLg1Component = this.getRecordComponent(PartialCodesList.class, "labelLg1");
        assertNotNull(labelLg1Component);
        
        assertTrue(labelLg1Component.isAnnotationPresent(Predicate.class));
        assertTrue(labelLg1Component.isAnnotationPresent(DefaultSortField.class));
        
        final Predicate predicate = labelLg1Component.getAnnotation(Predicate.class);
        assertEquals("skos:prefLabel", predicate.value());
        assertFalse(predicate.optional());
        
        final RecordComponent labelLg2Component = this.getRecordComponent(PartialCodesList.class, "labelLg2");
        assertNotNull(labelLg2Component);
        
        assertTrue(labelLg2Component.isAnnotationPresent(Predicate.class));
        assertFalse(labelLg2Component.isAnnotationPresent(DefaultSortField.class));
        
        final Predicate predicate2 = labelLg2Component.getAnnotation(Predicate.class);
        assertEquals("skos:prefLabel", predicate2.value());
        assertTrue(predicate2.optional());
    }

    @Test
    void shouldHaveCorrectRangeAnnotation() throws Exception {
        final RecordComponent rangeComponent = this.getRecordComponent(PartialCodesList.class, "range");
        assertNotNull(rangeComponent);
        
        assertTrue(rangeComponent.isAnnotationPresent(Predicate.class));
        final Predicate predicate = rangeComponent.getAnnotation(Predicate.class);
        assertEquals("rdfs:seeAlso", predicate.value());
        assertTrue(predicate.optional());
    }

    @Test
    void shouldHaveOnlyOneDefaultSortField() {
        final RecordComponent[] components = PartialCodesList.class.getRecordComponents();
        int defaultSortFieldCount = 0;
        String defaultSortFieldName = null;
        
        for (final RecordComponent component : components) {
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
        final PartialCodesList partialCodesList = new PartialCodesList(
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
        final PartialCodesList partialCodesList = new PartialCodesList(null, null, null, null, null);
        
        assertNotNull(partialCodesList);
        assertNull(partialCodesList.id());
        assertNull(partialCodesList.uri());
        assertNull(partialCodesList.labelLg1());
        assertNull(partialCodesList.labelLg2());
        assertNull(partialCodesList.range());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        final PartialCodesList partialCodesList1 = new PartialCodesList("PARTIAL001", "http://example.com/test", "Label1", "Label2", "Range");
        final PartialCodesList partialCodesList2 = new PartialCodesList("PARTIAL001", "http://example.com/test", "Label1", "Label2", "Range");
        final PartialCodesList partialCodesList3 = new PartialCodesList("PARTIAL002", "http://example.com/test2", "Label3", "Label4", "Range2");
        
        assertEquals(partialCodesList1, partialCodesList2);
        assertNotEquals(partialCodesList1, partialCodesList3);
        assertEquals(partialCodesList1.hashCode(), partialCodesList2.hashCode());
    }

    @Test
    void shouldImplementToString() {
        final PartialCodesList partialCodesList = new PartialCodesList("PARTIAL001", "http://example.com/test", "Label1", "Label2", "Range");
        final String toString = partialCodesList.toString();
        
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
        final RecordComponent idComponent = this.getRecordComponent(PartialCodesList.class, "id");
        assertEquals(String.class, idComponent.getType());
        
        final RecordComponent uriComponent = this.getRecordComponent(PartialCodesList.class, "uri");
        assertEquals(String.class, uriComponent.getType());
        
        final RecordComponent labelLg1Component = this.getRecordComponent(PartialCodesList.class, "labelLg1");
        assertEquals(String.class, labelLg1Component.getType());
        
        final RecordComponent labelLg2Component = this.getRecordComponent(PartialCodesList.class, "labelLg2");
        assertEquals(String.class, labelLg2Component.getType());
        
        final RecordComponent rangeComponent = this.getRecordComponent(PartialCodesList.class, "range");
        assertEquals(String.class, rangeComponent.getType());
    }

    @Test
    void shouldHaveOptionalFieldsMarkedCorrectly() throws Exception {
        final RecordComponent labelLg1Component = this.getRecordComponent(PartialCodesList.class, "labelLg1");
        final Predicate labelLg1Predicate = labelLg1Component.getAnnotation(Predicate.class);
        assertNotNull(labelLg1Predicate);
        assertFalse(labelLg1Predicate.optional());
        
        final RecordComponent labelLg2Component = this.getRecordComponent(PartialCodesList.class, "labelLg2");
        final Predicate labelLg2Predicate = labelLg2Component.getAnnotation(Predicate.class);
        assertNotNull(labelLg2Predicate);
        assertTrue(labelLg2Predicate.optional());
        
        final RecordComponent rangeComponent = this.getRecordComponent(PartialCodesList.class, "range");
        final Predicate rangePredicate = rangeComponent.getAnnotation(Predicate.class);
        assertNotNull(rangePredicate);
        assertTrue(rangePredicate.optional());
    }

    @Test
    void shouldHandlePartiallyNullFields() {
        final PartialCodesList partialCodesList = new PartialCodesList(
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

    private RecordComponent getRecordComponent(final Class<?> recordClass, final String componentName) {
        final RecordComponent[] components = recordClass.getRecordComponents();
        for (final RecordComponent component : components) {
            if (component.getName().equals(componentName)) {
                return component;
            }
        }
        return null;
    }
}