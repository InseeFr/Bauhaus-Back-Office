package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.graphdb.annotations.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.*;

class CodesListTest {

    @Test
    void shouldHaveCorrectClassAnnotations() {
        assertTrue(CodesList.class.isAnnotationPresent(Entity.class));
        assertTrue(CodesList.class.isAnnotationPresent(Graph.class));
        
        Entity entityAnnotation = CodesList.class.getAnnotation(Entity.class);
        assertEquals("skos:ConceptScheme", entityAnnotation.type());
        assertEquals("", entityAnnotation.value());
        
        Graph graphAnnotation = CodesList.class.getAnnotation(Graph.class);
        assertEquals("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}", 
                    graphAnnotation.value());
    }

    @Test
    void shouldHaveCorrectRecordComponents() {
        RecordComponent[] components = CodesList.class.getRecordComponents();
        assertEquals(5, components.length);
        
        // Check component names
        String[] expectedComponents = {"id", "uri", "labelLg1", "labelLg2", "range"};
        for (String expected : expectedComponents) {
            RecordComponent component = getRecordComponent(CodesList.class, expected);
            assertNotNull(component, "Missing component: " + expected);
        }
    }

    @Test
    void shouldHaveCorrectIdAnnotation() throws Exception {
        RecordComponent idComponent = getRecordComponent(CodesList.class, "id");
        assertNotNull(idComponent);
        
        assertTrue(idComponent.isAnnotationPresent(Predicate.class));
        Predicate predicate = idComponent.getAnnotation(Predicate.class);
        assertEquals("skos:notation", predicate.value());
        assertEquals("", predicate.namespace()); // Default namespace
    }

    @Test
    void shouldHaveCorrectUriAnnotation() throws Exception {
        RecordComponent uriComponent = getRecordComponent(CodesList.class, "uri");
        assertNotNull(uriComponent);
        
        assertTrue(uriComponent.isAnnotationPresent(Statement.class));
        assertFalse(uriComponent.isAnnotationPresent(Predicate.class));
    }

    @Test
    void shouldHaveCorrectLabelAnnotations() throws Exception {
        RecordComponent labelLg1Component = getRecordComponent(CodesList.class, "labelLg1");
        assertNotNull(labelLg1Component);
        
        assertTrue(labelLg1Component.isAnnotationPresent(Predicate.class));
        assertTrue(labelLg1Component.isAnnotationPresent(DefaultSortField.class));
        
        Predicate predicate = labelLg1Component.getAnnotation(Predicate.class);
        assertEquals("skos:prefLabel", predicate.value());
        
        RecordComponent labelLg2Component = getRecordComponent(CodesList.class, "labelLg2");
        assertNotNull(labelLg2Component);
        
        assertTrue(labelLg2Component.isAnnotationPresent(Predicate.class));
        assertFalse(labelLg2Component.isAnnotationPresent(DefaultSortField.class));
        
        Predicate predicate2 = labelLg2Component.getAnnotation(Predicate.class);
        assertEquals("skos:prefLabel", predicate2.value());
    }

    @Test
    void shouldHaveCorrectRangeAnnotation() throws Exception {
        RecordComponent rangeComponent = getRecordComponent(CodesList.class, "range");
        assertNotNull(rangeComponent);
        
        assertTrue(rangeComponent.isAnnotationPresent(Predicate.class));
        Predicate predicate = rangeComponent.getAnnotation(Predicate.class);
        assertEquals("rdfs:seeAlso", predicate.value());
    }

    @Test
    void shouldHaveOnlyOneDefaultSortField() {
        RecordComponent[] components = CodesList.class.getRecordComponents();
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
        CodesList codesList = new CodesList(
            "TEST001",
            "http://example.com/codelist/TEST001",
            "Test Label FR",
            "Test Label EN",
            "http://example.com/range"
        );
        
        assertNotNull(codesList);
        assertEquals("TEST001", codesList.id());
        assertEquals("http://example.com/codelist/TEST001", codesList.uri());
        assertEquals("Test Label FR", codesList.labelLg1());
        assertEquals("Test Label EN", codesList.labelLg2());
        assertEquals("http://example.com/range", codesList.range());
    }

    @Test
    void shouldHandleNullValues() {
        CodesList codesList = new CodesList(null, null, null, null, null);
        
        assertNotNull(codesList);
        assertNull(codesList.id());
        assertNull(codesList.uri());
        assertNull(codesList.labelLg1());
        assertNull(codesList.labelLg2());
        assertNull(codesList.range());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        CodesList codesList1 = new CodesList("TEST001", "http://example.com/test", "Label1", "Label2", "Range");
        CodesList codesList2 = new CodesList("TEST001", "http://example.com/test", "Label1", "Label2", "Range");
        CodesList codesList3 = new CodesList("TEST002", "http://example.com/test2", "Label3", "Label4", "Range2");
        
        assertEquals(codesList1, codesList2);
        assertNotEquals(codesList1, codesList3);
        assertEquals(codesList1.hashCode(), codesList2.hashCode());
    }

    @Test
    void shouldImplementToString() {
        CodesList codesList = new CodesList("TEST001", "http://example.com/test", "Label1", "Label2", "Range");
        String toString = codesList.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("TEST001"));
        assertTrue(toString.contains("http://example.com/test"));
        assertTrue(toString.contains("Label1"));
        assertTrue(toString.contains("Label2"));
        assertTrue(toString.contains("Range"));
    }

    @Test
    void shouldBeRecord() {
        assertTrue(CodesList.class.isRecord());
    }

    @Test
    void shouldHaveCorrectComponentTypes() throws Exception {
        RecordComponent idComponent = getRecordComponent(CodesList.class, "id");
        assertEquals(String.class, idComponent.getType());
        
        RecordComponent uriComponent = getRecordComponent(CodesList.class, "uri");
        assertEquals(String.class, uriComponent.getType());
        
        RecordComponent labelLg1Component = getRecordComponent(CodesList.class, "labelLg1");
        assertEquals(String.class, labelLg1Component.getType());
        
        RecordComponent labelLg2Component = getRecordComponent(CodesList.class, "labelLg2");
        assertEquals(String.class, labelLg2Component.getType());
        
        RecordComponent rangeComponent = getRecordComponent(CodesList.class, "range");
        assertEquals(String.class, rangeComponent.getType());
    }
    
    // Helper method to get record component by name
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