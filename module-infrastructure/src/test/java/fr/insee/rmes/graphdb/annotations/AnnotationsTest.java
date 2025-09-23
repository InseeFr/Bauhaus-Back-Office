package fr.insee.rmes.graphdb.annotations;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationsTest {

    // Test record for annotation testing
    @Entity(value = "TestEntity", type = "test:Type")
    @Graph("http://test.graph")
    public record TestRecord(
            @Statement
            String uri,
            
            @Predicate(value = "test:property", namespace = "http://test.namespace")
            String property,
            
            @DefaultSortField
            String sortField,
            
            String normalField
    ) {}

    // Test class for field annotations
    @SuppressWarnings("unused")
    public static class TestClass {
        @Statement
        private String uriField;
        
        @Predicate(value = "test:prop")
        private String predicateField;
        
        @DefaultSortField
        private String sortField;
        
        private String normalField;
    }

    @Test
    void entityAnnotationShouldBeRuntimeRetained() throws Exception {
        Entity entityAnnotation = TestRecord.class.getAnnotation(Entity.class);
        assertNotNull(entityAnnotation);
        assertEquals("TestEntity", entityAnnotation.value());
        assertEquals("test:Type", entityAnnotation.type());
        
        // Verify annotation metadata
        Target target = Entity.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(containsElementType(target.value(), ElementType.TYPE));
        
        Retention retention = Entity.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void graphAnnotationShouldBeRuntimeRetained() throws Exception {
        Graph graphAnnotation = TestRecord.class.getAnnotation(Graph.class);
        assertNotNull(graphAnnotation);
        assertEquals("http://test.graph", graphAnnotation.value());
        
        // Verify annotation metadata
        Target target = Graph.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(containsElementType(target.value(), ElementType.TYPE));
        
        Retention retention = Graph.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void statementAnnotationShouldWorkOnRecordComponents() throws Exception {
        RecordComponent uriComponent = getRecordComponent(TestRecord.class, "uri");
        assertNotNull(uriComponent);
        
        Statement statementAnnotation = uriComponent.getAnnotation(Statement.class);
        assertNotNull(statementAnnotation);
        
        // Verify annotation metadata
        Target target = Statement.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(containsElementType(target.value(), ElementType.FIELD));
        assertTrue(containsElementType(target.value(), ElementType.RECORD_COMPONENT));
        
        Retention retention = Statement.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void statementAnnotationShouldWorkOnFields() throws Exception {
        Field uriField = TestClass.class.getDeclaredField("uriField");
        assertNotNull(uriField);
        
        Statement statementAnnotation = uriField.getAnnotation(Statement.class);
        assertNotNull(statementAnnotation);
    }

    @Test
    void predicateAnnotationShouldWorkOnRecordComponents() throws Exception {
        RecordComponent propertyComponent = getRecordComponent(TestRecord.class, "property");
        assertNotNull(propertyComponent);
        
        Predicate predicateAnnotation = propertyComponent.getAnnotation(Predicate.class);
        assertNotNull(predicateAnnotation);
        assertEquals("test:property", predicateAnnotation.value());
        assertEquals("http://test.namespace", predicateAnnotation.namespace());
        
        // Verify annotation metadata
        Target target = Predicate.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(containsElementType(target.value(), ElementType.FIELD));
        assertTrue(containsElementType(target.value(), ElementType.PARAMETER));
        assertTrue(containsElementType(target.value(), ElementType.RECORD_COMPONENT));
        
        Retention retention = Predicate.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void predicateAnnotationShouldHaveDefaultNamespace() throws Exception {
        Field predicateField = TestClass.class.getDeclaredField("predicateField");
        assertNotNull(predicateField);
        
        Predicate predicateAnnotation = predicateField.getAnnotation(Predicate.class);
        assertNotNull(predicateAnnotation);
        assertEquals("test:prop", predicateAnnotation.value());
        assertEquals("", predicateAnnotation.namespace()); // Default empty namespace
    }

    @Test
    void defaultSortFieldAnnotationShouldWorkOnRecordComponents() throws Exception {
        RecordComponent sortComponent = getRecordComponent(TestRecord.class, "sortField");
        assertNotNull(sortComponent);
        
        DefaultSortField sortAnnotation = sortComponent.getAnnotation(DefaultSortField.class);
        assertNotNull(sortAnnotation);
        
        // Verify annotation metadata
        Target target = DefaultSortField.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(containsElementType(target.value(), ElementType.FIELD));
        assertTrue(containsElementType(target.value(), ElementType.RECORD_COMPONENT));
        
        Retention retention = DefaultSortField.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void defaultSortFieldAnnotationShouldWorkOnFields() throws Exception {
        Field sortField = TestClass.class.getDeclaredField("sortField");
        assertNotNull(sortField);
        
        DefaultSortField sortAnnotation = sortField.getAnnotation(DefaultSortField.class);
        assertNotNull(sortAnnotation);
    }

    @Test
    void typeAnnotationShouldWorkOnClasses() throws Exception {
        // Test with a class that has @Type annotation
        @Type("test:CustomType")
        class TypedClass {}
        
        Type typeAnnotation = TypedClass.class.getAnnotation(Type.class);
        assertNotNull(typeAnnotation);
        assertEquals("test:CustomType", typeAnnotation.value());
        
        // Verify annotation metadata
        Target target = Type.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(containsElementType(target.value(), ElementType.TYPE));
        
        Retention retention = Type.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void entityAnnotationShouldHaveDefaultValues() {
        // Test with annotation that uses defaults
        @Entity
        record DefaultEntity() {}
        
        Entity entityAnnotation = DefaultEntity.class.getAnnotation(Entity.class);
        assertNotNull(entityAnnotation);
        assertEquals("", entityAnnotation.value());
        assertEquals("", entityAnnotation.type());
    }

    @Test
    void allAnnotationsShouldBePresent() throws Exception {
        // Verify all expected annotations are present on test record
        assertTrue(TestRecord.class.isAnnotationPresent(Entity.class));
        assertTrue(TestRecord.class.isAnnotationPresent(Graph.class));
        
        RecordComponent[] components = TestRecord.class.getRecordComponents();
        assertEquals(4, components.length);
        
        // Check each component has expected annotation
        RecordComponent uriComp = getRecordComponent(TestRecord.class, "uri");
        assertTrue(uriComp.isAnnotationPresent(Statement.class));
        
        RecordComponent propComp = getRecordComponent(TestRecord.class, "property");
        assertTrue(propComp.isAnnotationPresent(Predicate.class));
        
        RecordComponent sortComp = getRecordComponent(TestRecord.class, "sortField");
        assertTrue(sortComp.isAnnotationPresent(DefaultSortField.class));
        
        RecordComponent normalComp = getRecordComponent(TestRecord.class, "normalField");
        assertFalse(normalComp.isAnnotationPresent(Statement.class));
        assertFalse(normalComp.isAnnotationPresent(Predicate.class));
        assertFalse(normalComp.isAnnotationPresent(DefaultSortField.class));
    }

    @Test
    void annotationsShouldWorkWithReflection() throws Exception {
        // Test that annotations can be discovered via reflection
        Annotation[] classAnnotations = TestRecord.class.getAnnotations();
        assertTrue(classAnnotations.length >= 2);
        
        boolean hasEntity = false;
        boolean hasGraph = false;
        
        for (Annotation annotation : classAnnotations) {
            if (annotation instanceof Entity) {
                hasEntity = true;
            } else if (annotation instanceof Graph) {
                hasGraph = true;
            }
        }
        
        assertTrue(hasEntity);
        assertTrue(hasGraph);
    }

    @Test
    void shouldHandleMethodParameters() throws Exception {
        // Test method with Predicate annotation on parameter
        class TestMethodClass {
            @SuppressWarnings("unused")
            public void testMethod(@Predicate(value = "test:param") String param) {}
        }
        
        Method method = TestMethodClass.class.getMethod("testMethod", String.class);
        assertNotNull(method);
        
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        assertEquals(1, paramAnnotations.length);
        assertEquals(1, paramAnnotations[0].length);
        assertTrue(paramAnnotations[0][0] instanceof Predicate);
        
        Predicate predicateAnnotation = (Predicate) paramAnnotations[0][0];
        assertEquals("test:param", predicateAnnotation.value());
    }

    // Helper method to check if ElementType array contains specific type
    private boolean containsElementType(ElementType[] types, ElementType target) {
        for (ElementType type : types) {
            if (type == target) {
                return true;
            }
        }
        return false;
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