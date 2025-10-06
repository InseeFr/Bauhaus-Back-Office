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
    void entityAnnotationShouldBeRuntimeRetained() {
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
    void graphAnnotationShouldBeRuntimeRetained() {
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
    void statementAnnotationShouldWorkOnRecordComponents() {
        RecordComponent uriComponent = getRecordComponent(TestRecord.class, "uri");
        assertNotNull(uriComponent);
        
        Statement statementAnnotation = uriComponent.getAnnotation(Statement.class);
        assertNotNull(statementAnnotation);
        
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
    void predicateAnnotationShouldWorkOnRecordComponents() {
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
    void defaultSortFieldAnnotationShouldWorkOnRecordComponents() {
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
    void typeAnnotationShouldWorkOnClasses() {
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
    void allAnnotationsShouldBePresent() {
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
    void annotationsShouldWorkWithReflection() {
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

    @Test
    void predicateAnnotationShouldHaveDefaultOptionalValue() {
        // Test that @Predicate has default optional=true
        @SuppressWarnings("unused")
        record TestRecordWithDefaultOptional(
            @Predicate(value = "test:property")
            String defaultOptionalField
        ) {}
        
        RecordComponent component = getRecordComponent(TestRecordWithDefaultOptional.class, "defaultOptionalField");
        assertNotNull(component);
        
        Predicate predicate = component.getAnnotation(Predicate.class);
        assertNotNull(predicate);
        assertFalse(predicate.optional()); // Default should be true
    }

    @Test
    void predicateAnnotationShouldSupportOptionalFalse() {
        // Test explicit optional=false
        @SuppressWarnings("unused")
        record TestRecordWithMandatory(
            @Predicate(value = "test:property", optional = false)
            String mandatoryField
        ) {}
        
        RecordComponent component = getRecordComponent(TestRecordWithMandatory.class, "mandatoryField");
        assertNotNull(component);
        
        Predicate predicate = component.getAnnotation(Predicate.class);
        assertNotNull(predicate);
        assertFalse(predicate.optional());
    }

    @Test
    void predicateAnnotationShouldSupportOptionalTrue() {
        // Test explicit optional=true
        @SuppressWarnings("unused")
        record TestRecordWithOptional(
            @Predicate(value = "test:property", optional = true)
            String optionalField
        ) {}
        
        RecordComponent component = getRecordComponent(TestRecordWithOptional.class, "optionalField");
        assertNotNull(component);
        
        Predicate predicate = component.getAnnotation(Predicate.class);
        assertNotNull(predicate);
        assertTrue(predicate.optional());
    }

    @Test
    void predicateAnnotationShouldHaveDefaultInverseValue() {
        // Test that @Predicate has default inverse=false
        @SuppressWarnings("unused")
        record TestRecordWithDefaultInverse(
            @Predicate(value = "test:property")
            String defaultInverseField
        ) {}
        
        RecordComponent component = getRecordComponent(TestRecordWithDefaultInverse.class, "defaultInverseField");
        assertNotNull(component);
        
        Predicate predicate = component.getAnnotation(Predicate.class);
        assertNotNull(predicate);
        assertFalse(predicate.inverse()); // Default should be false
    }

    @Test
    void predicateAnnotationShouldSupportInverseTrue() {
        // Test explicit inverse=true
        @SuppressWarnings("unused")
        record TestRecordWithInverse(
            @Predicate(value = "test:property", inverse = true)
            String inverseField
        ) {}
        
        RecordComponent component = getRecordComponent(TestRecordWithInverse.class, "inverseField");
        assertNotNull(component);
        
        Predicate predicate = component.getAnnotation(Predicate.class);
        assertNotNull(predicate);
        assertTrue(predicate.inverse());
    }

    @Test
    void predicateAnnotationShouldSupportInverseFalse() {
        // Test explicit inverse=false
        @SuppressWarnings("unused")
        record TestRecordWithNormalDirection(
            @Predicate(value = "test:property", inverse = false)
            String normalField
        ) {}
        
        RecordComponent component = getRecordComponent(TestRecordWithNormalDirection.class, "normalField");
        assertNotNull(component);
        
        Predicate predicate = component.getAnnotation(Predicate.class);
        assertNotNull(predicate);
        assertFalse(predicate.inverse());
    }

    @Test
    void predicateAnnotationShouldSupportAllCombinations() {
        // Test all combinations of optional and inverse
        @SuppressWarnings("unused")
        record TestRecordWithAllCombinations(
            @Predicate(value = "test:prop1", optional = true, inverse = true)
            String optionalInverse,
            
            @Predicate(value = "test:prop2", optional = true, inverse = false)
            String optionalNormal,
            
            @Predicate(value = "test:prop3", optional = false, inverse = true)
            String mandatoryInverse,
            
            @Predicate(value = "test:prop4", optional = false, inverse = false)
            String mandatoryNormal
        ) {}
        
        // Test optional=true, inverse=true
        RecordComponent comp1 = getRecordComponent(TestRecordWithAllCombinations.class, "optionalInverse");
        Predicate pred1 = comp1.getAnnotation(Predicate.class);
        assertTrue(pred1.optional());
        assertTrue(pred1.inverse());
        
        // Test optional=true, inverse=false
        RecordComponent comp2 = getRecordComponent(TestRecordWithAllCombinations.class, "optionalNormal");
        Predicate pred2 = comp2.getAnnotation(Predicate.class);
        assertTrue(pred2.optional());
        assertFalse(pred2.inverse());
        
        // Test optional=false, inverse=true
        RecordComponent comp3 = getRecordComponent(TestRecordWithAllCombinations.class, "mandatoryInverse");
        Predicate pred3 = comp3.getAnnotation(Predicate.class);
        assertFalse(pred3.optional());
        assertTrue(pred3.inverse());
        
        // Test optional=false, inverse=false
        RecordComponent comp4 = getRecordComponent(TestRecordWithAllCombinations.class, "mandatoryNormal");
        Predicate pred4 = comp4.getAnnotation(Predicate.class);
        assertFalse(pred4.optional());
        assertFalse(pred4.inverse());
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