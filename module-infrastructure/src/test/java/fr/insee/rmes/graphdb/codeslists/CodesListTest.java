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
        assertEquals(14, components.length);
        
        // Check component names
        String[] expectedComponents = {"uri", "id", "labelLg1", "labelLg2", "descriptionLg1", "descriptionLg2", "range", "lastCodeUriSegment", "created", "creator", "validationState", "disseminationStatus", "modified", "iriParent"};
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
        assertEquals("", predicate.lang()); // Default lang
        assertFalse(predicate.optional()); // Default optional
        assertFalse(predicate.inverse()); // Default inverse
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
        assertEquals("lg1", predicate.lang());
        assertFalse(predicate.optional());
        
        RecordComponent labelLg2Component = getRecordComponent(CodesList.class, "labelLg2");
        assertNotNull(labelLg2Component);
        
        assertTrue(labelLg2Component.isAnnotationPresent(Predicate.class));
        assertFalse(labelLg2Component.isAnnotationPresent(DefaultSortField.class));
        
        Predicate predicate2 = labelLg2Component.getAnnotation(Predicate.class);
        assertEquals("skos:prefLabel", predicate2.value());
        assertEquals("lg2", predicate2.lang());
        assertTrue(predicate2.optional());
    }

    @Test
    void shouldHaveCorrectRangeAnnotation() throws Exception {
        RecordComponent rangeComponent = getRecordComponent(CodesList.class, "range");
        assertNotNull(rangeComponent);
        
        assertTrue(rangeComponent.isAnnotationPresent(Predicate.class));
        Predicate predicate = rangeComponent.getAnnotation(Predicate.class);
        assertEquals("rdfs:seeAlso", predicate.value());
        assertTrue(predicate.optional());
        assertTrue(predicate.inverse());
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
            "http://example.com/codelist/TEST001",
            "TEST001",
            "Test Label FR",
            "Test Label EN",
            "Description FR",
            "Description EN",
            "http://example.com/range",
            "LAST001",
            "2023-01-01",
            "creator1",
            "VALIDATED",
            "PUBLIC",
            "2023-01-02",
            "http://example.com/parent"
        );
        
        assertNotNull(codesList);
        assertEquals("http://example.com/codelist/TEST001", codesList.uri());
        assertEquals("TEST001", codesList.id());
        assertEquals("Test Label FR", codesList.labelLg1());
        assertEquals("Test Label EN", codesList.labelLg2());
        assertEquals("Description FR", codesList.descriptionLg1());
        assertEquals("Description EN", codesList.descriptionLg2());
        assertEquals("http://example.com/range", codesList.range());
        assertEquals("LAST001", codesList.lastCodeUriSegment());
        assertEquals("2023-01-01", codesList.created());
        assertEquals("creator1", codesList.creator());
        assertEquals("VALIDATED", codesList.validationState());
        assertEquals("PUBLIC", codesList.disseminationStatus());
        assertEquals("2023-01-02", codesList.modified());
        assertEquals("http://example.com/parent", codesList.iriParent());
    }

    @Test
    void shouldHandleNullValues() {
        CodesList codesList = new CodesList(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        
        assertNotNull(codesList);
        assertNull(codesList.uri());
        assertNull(codesList.id());
        assertNull(codesList.labelLg1());
        assertNull(codesList.labelLg2());
        assertNull(codesList.descriptionLg1());
        assertNull(codesList.descriptionLg2());
        assertNull(codesList.range());
        assertNull(codesList.lastCodeUriSegment());
        assertNull(codesList.created());
        assertNull(codesList.creator());
        assertNull(codesList.validationState());
        assertNull(codesList.disseminationStatus());
        assertNull(codesList.modified());
        assertNull(codesList.iriParent());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        CodesList codesList1 = new CodesList("http://example.com/test", "TEST001", "Label1", "Label2", "Desc1", "Desc2", "Range", "LAST001", "2023-01-01", "creator1", "VALIDATED", "PUBLIC", "2023-01-02", "http://example.com/parent");
        CodesList codesList2 = new CodesList("http://example.com/test", "TEST001", "Label1", "Label2", "Desc1", "Desc2", "Range", "LAST001", "2023-01-01", "creator1", "VALIDATED", "PUBLIC", "2023-01-02", "http://example.com/parent");
        CodesList codesList3 = new CodesList("http://example.com/test2", "TEST002", "Label3", "Label4", "Desc3", "Desc4", "Range2", "LAST002", "2023-01-03", "creator2", "DRAFT", "PRIVATE", "2023-01-04", "http://example.com/parent2");
        
        assertEquals(codesList1, codesList2);
        assertNotEquals(codesList1, codesList3);
        assertEquals(codesList1.hashCode(), codesList2.hashCode());
    }

    @Test
    void shouldImplementToString() {
        CodesList codesList = new CodesList("http://example.com/test", "TEST001", "Label1", "Label2", "Desc1", "Desc2", "Range", "LAST001", "2023-01-01", "creator1", "VALIDATED", "PUBLIC", "2023-01-02", "http://example.com/parent");
        String toString = codesList.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("http://example.com/test"));
        assertTrue(toString.contains("TEST001"));
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
        String[] componentNames = {"uri", "id", "labelLg1", "labelLg2", "descriptionLg1", "descriptionLg2", "range", "lastCodeUriSegment", "created", "creator", "validationState", "disseminationStatus", "modified", "iriParent"};
        
        for (String componentName : componentNames) {
            RecordComponent component = getRecordComponent(CodesList.class, componentName);
            assertNotNull(component, "Component " + componentName + " should exist");
            assertEquals(String.class, component.getType(), "Component " + componentName + " should be of type String");
        }
    }

    @Test
    void shouldHaveCorrectDescriptionAnnotations() throws Exception {
        RecordComponent descriptionLg1Component = getRecordComponent(CodesList.class, "descriptionLg1");
        assertNotNull(descriptionLg1Component);
        
        assertTrue(descriptionLg1Component.isAnnotationPresent(Predicate.class));
        Predicate predicate = descriptionLg1Component.getAnnotation(Predicate.class);
        assertEquals("skos:definition", predicate.value());
        assertEquals("lg1", predicate.lang());
        assertTrue(predicate.optional());
        
        RecordComponent descriptionLg2Component = getRecordComponent(CodesList.class, "descriptionLg2");
        assertNotNull(descriptionLg2Component);
        
        assertTrue(descriptionLg2Component.isAnnotationPresent(Predicate.class));
        Predicate predicate2 = descriptionLg2Component.getAnnotation(Predicate.class);
        assertEquals("skos:definition", predicate2.value());
        assertEquals("lg2", predicate2.lang());
        assertTrue(predicate2.optional());
    }

    @Test
    void shouldHaveCorrectOptionalFieldAnnotations() throws Exception {
        String[] optionalFields = {"lastCodeUriSegment", "created", "creator", "validationState", "disseminationStatus", "modified", "iriParent"};
        String[] expectedPredicates = {"insee:lastCodeUriSegment", "dcterms:created", "dc:creator", "insee:validationState", "insee:disseminationStatus", "dcterms:modified", "prov:wasDerivedFrom"};
        
        for (int i = 0; i < optionalFields.length; i++) {
            RecordComponent component = getRecordComponent(CodesList.class, optionalFields[i]);
            assertNotNull(component, "Component " + optionalFields[i] + " should exist");
            
            assertTrue(component.isAnnotationPresent(Predicate.class), 
                "Component " + optionalFields[i] + " should have @Predicate annotation");
            
            Predicate predicate = component.getAnnotation(Predicate.class);
            assertEquals(expectedPredicates[i], predicate.value(), 
                "Component " + optionalFields[i] + " should have correct predicate value");
            assertTrue(predicate.optional(), 
                "Component " + optionalFields[i] + " should be optional");
        }
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