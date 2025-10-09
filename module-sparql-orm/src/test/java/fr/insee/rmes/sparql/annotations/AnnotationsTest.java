package fr.insee.rmes.sparql.annotations;

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
        
        @Predicate("test:prop")
        private String predicateField;
        
        @DefaultSortField
        private String sortField;
        
        private String normalField;
    }

    @Test
    void entityAnnotationShouldBeRuntimeRetained() {
        final Entity entityAnnotation = TestRecord.class.getAnnotation(Entity.class);
        assertNotNull(entityAnnotation);
        assertEquals("TestEntity", entityAnnotation.value());
        assertEquals("test:Type", entityAnnotation.type());
        
        // Verify annotation metadata
        final Target target = Entity.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(this.containsElementType(target.value(), ElementType.TYPE));
        
        final Retention retention = Entity.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void graphAnnotationShouldBeRuntimeRetained() {
        final Graph graphAnnotation = TestRecord.class.getAnnotation(Graph.class);
        assertNotNull(graphAnnotation);
        assertEquals("http://test.graph", graphAnnotation.value());
        
        // Verify annotation metadata
        final Target target = Graph.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(this.containsElementType(target.value(), ElementType.TYPE));
        
        final Retention retention = Graph.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void statementAnnotationShouldWorkOnRecordComponents() {
        final RecordComponent uriComponent = this.getRecordComponent(TestRecord.class, "uri");
        assertNotNull(uriComponent);
        
        final Statement statementAnnotation = uriComponent.getAnnotation(Statement.class);
        assertNotNull(statementAnnotation);
        
        final Target target = Statement.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(this.containsElementType(target.value(), ElementType.FIELD));
        assertTrue(this.containsElementType(target.value(), ElementType.RECORD_COMPONENT));
        
        final Retention retention = Statement.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void statementAnnotationShouldWorkOnFields() throws Exception {
        final Field uriField = TestClass.class.getDeclaredField("uriField");
        assertNotNull(uriField);
        
        final Statement statementAnnotation = uriField.getAnnotation(Statement.class);
        assertNotNull(statementAnnotation);
    }

    @Test
    void predicateAnnotationShouldWorkOnRecordComponents() {
        final RecordComponent propertyComponent = this.getRecordComponent(TestRecord.class, "property");
        assertNotNull(propertyComponent);
        
        final Predicate predicateAnnotation = propertyComponent.getAnnotation(Predicate.class);
        assertNotNull(predicateAnnotation);
        assertEquals("test:property", predicateAnnotation.value());
        assertEquals("http://test.namespace", predicateAnnotation.namespace());
        
        // Verify annotation metadata
        final Target target = Predicate.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(this.containsElementType(target.value(), ElementType.FIELD));
        assertTrue(this.containsElementType(target.value(), ElementType.PARAMETER));
        assertTrue(this.containsElementType(target.value(), ElementType.RECORD_COMPONENT));
        
        final Retention retention = Predicate.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void predicateAnnotationShouldHaveDefaultNamespace() throws Exception {
        final Field predicateField = TestClass.class.getDeclaredField("predicateField");
        assertNotNull(predicateField);
        
        final Predicate predicateAnnotation = predicateField.getAnnotation(Predicate.class);
        assertNotNull(predicateAnnotation);
        assertEquals("test:prop", predicateAnnotation.value());
        assertEquals("", predicateAnnotation.namespace()); // Default empty namespace
    }

    @Test
    void defaultSortFieldAnnotationShouldWorkOnRecordComponents() {
        final RecordComponent sortComponent = this.getRecordComponent(TestRecord.class, "sortField");
        assertNotNull(sortComponent);
        
        final DefaultSortField sortAnnotation = sortComponent.getAnnotation(DefaultSortField.class);
        assertNotNull(sortAnnotation);
        
        // Verify annotation metadata
        final Target target = DefaultSortField.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(this.containsElementType(target.value(), ElementType.FIELD));
        assertTrue(this.containsElementType(target.value(), ElementType.RECORD_COMPONENT));
        
        final Retention retention = DefaultSortField.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void defaultSortFieldAnnotationShouldWorkOnFields() throws Exception {
        final Field sortField = TestClass.class.getDeclaredField("sortField");
        assertNotNull(sortField);
        
        final DefaultSortField sortAnnotation = sortField.getAnnotation(DefaultSortField.class);
        assertNotNull(sortAnnotation);
    }

    @Test
    void typeAnnotationShouldWorkOnClasses() {
        // Test with a class that has @Type annotation
        @Type("test:CustomType")
        class TypedClass {}
        
        final Type typeAnnotation = TypedClass.class.getAnnotation(Type.class);
        assertNotNull(typeAnnotation);
        assertEquals("test:CustomType", typeAnnotation.value());
        
        // Verify annotation metadata
        final Target target = Type.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertTrue(this.containsElementType(target.value(), ElementType.TYPE));
        
        final Retention retention = Type.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void entityAnnotationShouldHaveDefaultValues() {
        // Test with annotation that uses defaults
        @Entity
        record DefaultEntity() {}
        
        final Entity entityAnnotation = DefaultEntity.class.getAnnotation(Entity.class);
        assertNotNull(entityAnnotation);
        assertEquals("", entityAnnotation.value());
        assertEquals("", entityAnnotation.type());
    }

    @Test
    void allAnnotationsShouldBePresent() {
        // Verify all expected annotations are present on test record
        assertTrue(TestRecord.class.isAnnotationPresent(Entity.class));
        assertTrue(TestRecord.class.isAnnotationPresent(Graph.class));
        
        final RecordComponent[] components = TestRecord.class.getRecordComponents();
        assertEquals(4, components.length);
        
        // Check each component has expected annotation
        final RecordComponent uriComp = this.getRecordComponent(TestRecord.class, "uri");
        assertTrue(uriComp.isAnnotationPresent(Statement.class));
        
        final RecordComponent propComp = this.getRecordComponent(TestRecord.class, "property");
        assertTrue(propComp.isAnnotationPresent(Predicate.class));
        
        final RecordComponent sortComp = this.getRecordComponent(TestRecord.class, "sortField");
        assertTrue(sortComp.isAnnotationPresent(DefaultSortField.class));
        
        final RecordComponent normalComp = this.getRecordComponent(TestRecord.class, "normalField");
        assertFalse(normalComp.isAnnotationPresent(Statement.class));
        assertFalse(normalComp.isAnnotationPresent(Predicate.class));
        assertFalse(normalComp.isAnnotationPresent(DefaultSortField.class));
    }

    @Test
    void annotationsShouldWorkWithReflection() {
        // Test that annotations can be discovered via reflection
        final Annotation[] classAnnotations = TestRecord.class.getAnnotations();
        assertTrue(2 <= classAnnotations.length);
        
        boolean hasEntity = false;
        boolean hasGraph = false;
        
        for (final Annotation annotation : classAnnotations) {
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
            public void testMethod(@Predicate("test:param") final String param) {}
        }
        
        final Method method = TestMethodClass.class.getMethod("testMethod", String.class);
        assertNotNull(method);
        
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        assertEquals(1, paramAnnotations.length);
        assertEquals(1, paramAnnotations[0].length);
        assertInstanceOf(Predicate.class, paramAnnotations[0][0]);
        
        final Predicate predicateAnnotation = (Predicate) paramAnnotations[0][0];
        assertEquals("test:param", predicateAnnotation.value());
    }

    @Test
    void predicateAnnotationShouldHaveDefaultOptionalValue() {
        // Test that @Predicate has default optional=true
        @SuppressWarnings("unused")
        record TestRecordWithDefaultOptional(
                @Predicate("test:property")
            String defaultOptionalField
        ) {}
        
        final RecordComponent component = this.getRecordComponent(TestRecordWithDefaultOptional.class, "defaultOptionalField");
        assertNotNull(component);
        
        final Predicate predicate = component.getAnnotation(Predicate.class);
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
        
        final RecordComponent component = this.getRecordComponent(TestRecordWithMandatory.class, "mandatoryField");
        assertNotNull(component);
        
        final Predicate predicate = component.getAnnotation(Predicate.class);
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
        
        final RecordComponent component = this.getRecordComponent(TestRecordWithOptional.class, "optionalField");
        assertNotNull(component);
        
        final Predicate predicate = component.getAnnotation(Predicate.class);
        assertNotNull(predicate);
        assertTrue(predicate.optional());
    }

    @Test
    void predicateAnnotationShouldHaveDefaultInverseValue() {
        // Test that @Predicate has default inverse=false
        @SuppressWarnings("unused")
        record TestRecordWithDefaultInverse(
                @Predicate("test:property")
            String defaultInverseField
        ) {}
        
        final RecordComponent component = this.getRecordComponent(TestRecordWithDefaultInverse.class, "defaultInverseField");
        assertNotNull(component);
        
        final Predicate predicate = component.getAnnotation(Predicate.class);
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
        
        final RecordComponent component = this.getRecordComponent(TestRecordWithInverse.class, "inverseField");
        assertNotNull(component);
        
        final Predicate predicate = component.getAnnotation(Predicate.class);
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
        
        final RecordComponent component = this.getRecordComponent(TestRecordWithNormalDirection.class, "normalField");
        assertNotNull(component);
        
        final Predicate predicate = component.getAnnotation(Predicate.class);
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
        final RecordComponent comp1 = this.getRecordComponent(TestRecordWithAllCombinations.class, "optionalInverse");
        final Predicate pred1 = comp1.getAnnotation(Predicate.class);
        assertTrue(pred1.optional());
        assertTrue(pred1.inverse());
        
        // Test optional=true, inverse=false
        final RecordComponent comp2 = this.getRecordComponent(TestRecordWithAllCombinations.class, "optionalNormal");
        final Predicate pred2 = comp2.getAnnotation(Predicate.class);
        assertTrue(pred2.optional());
        assertFalse(pred2.inverse());
        
        // Test optional=false, inverse=true
        final RecordComponent comp3 = this.getRecordComponent(TestRecordWithAllCombinations.class, "mandatoryInverse");
        final Predicate pred3 = comp3.getAnnotation(Predicate.class);
        assertFalse(pred3.optional());
        assertTrue(pred3.inverse());
        
        // Test optional=false, inverse=false
        final RecordComponent comp4 = this.getRecordComponent(TestRecordWithAllCombinations.class, "mandatoryNormal");
        final Predicate pred4 = comp4.getAnnotation(Predicate.class);
        assertFalse(pred4.optional());
        assertFalse(pred4.inverse());
    }

    // Helper method to check if ElementType array contains specific type
    private boolean containsElementType(final ElementType[] types, final ElementType target) {
        for (final ElementType type : types) {
            if (type == target) {
                return true;
            }
        }
        return false;
    }
    
    // Helper method to get record component by name
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