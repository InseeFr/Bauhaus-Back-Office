package fr.insee.rmes.graphdb.annotations;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PredicateTest {

    @Test
    void shouldHaveCorrectAnnotationProperties() throws NoSuchMethodException {
        // Test des propriétés de l'annotation
        Method valueMethod = Predicate.class.getMethod("value");
        Method namespaceMethod = Predicate.class.getMethod("namespace");
        Method optionalMethod = Predicate.class.getMethod("optional");
        Method inverseMethod = Predicate.class.getMethod("inverse");

        assertNotNull(valueMethod);
        assertNotNull(namespaceMethod);
        assertNotNull(optionalMethod);
        assertNotNull(inverseMethod);
    }

    @Test
    void shouldHaveCorrectDefaultValues() throws NoSuchMethodException {
        // Test des valeurs par défaut
        assertEquals("", (String) Predicate.class.getMethod("namespace").getDefaultValue());
        assertEquals(true, (Boolean) Predicate.class.getMethod("optional").getDefaultValue());
        assertEquals(false, (Boolean) Predicate.class.getMethod("inverse").getDefaultValue());
    }

    // Classe de test pour tester les annotations
    private static class TestEntity {
        @Predicate("skos:prefLabel")
        private String defaultPredicate;

        @Predicate(value = "skos:altLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
        private String predicateWithNamespace;

        @Predicate(value = "rdfs:label", optional = false)
        private String mandatoryPredicate;

        @Predicate(value = "rdfs:seeAlso", inverse = true)
        private String inversePredicate;

        @Predicate(value = "skos:notation", optional = false, inverse = false)
        private String explicitPredicate;

        @Predicate(value = "dcterms:hasPart", optional = true, inverse = true)
        private String optionalInversePredicate;
    }

    @Test
    void shouldReadDefaultPredicateAnnotation() throws NoSuchFieldException {
        var field = TestEntity.class.getDeclaredField("defaultPredicate");
        var predicate = field.getAnnotation(Predicate.class);

        assertNotNull(predicate);
        assertEquals("skos:prefLabel", predicate.value());
        assertEquals("", predicate.namespace());
        assertTrue(predicate.optional());
        assertFalse(predicate.inverse());
    }

    @Test
    void shouldReadPredicateWithNamespace() throws NoSuchFieldException {
        var field = TestEntity.class.getDeclaredField("predicateWithNamespace");
        var predicate = field.getAnnotation(Predicate.class);

        assertNotNull(predicate);
        assertEquals("skos:altLabel", predicate.value());
        assertEquals("http://www.w3.org/2004/02/skos/core#", predicate.namespace());
        assertTrue(predicate.optional());
        assertFalse(predicate.inverse());
    }

    @Test
    void shouldReadMandatoryPredicate() throws NoSuchFieldException {
        var field = TestEntity.class.getDeclaredField("mandatoryPredicate");
        var predicate = field.getAnnotation(Predicate.class);

        assertNotNull(predicate);
        assertEquals("rdfs:label", predicate.value());
        assertEquals("", predicate.namespace());
        assertFalse(predicate.optional());
        assertFalse(predicate.inverse());
    }

    @Test
    void shouldReadInversePredicate() throws NoSuchFieldException {
        var field = TestEntity.class.getDeclaredField("inversePredicate");
        var predicate = field.getAnnotation(Predicate.class);

        assertNotNull(predicate);
        assertEquals("rdfs:seeAlso", predicate.value());
        assertEquals("", predicate.namespace());
        assertTrue(predicate.optional());
        assertTrue(predicate.inverse());
    }

    @Test
    void shouldReadExplicitPredicate() throws NoSuchFieldException {
        var field = TestEntity.class.getDeclaredField("explicitPredicate");
        var predicate = field.getAnnotation(Predicate.class);

        assertNotNull(predicate);
        assertEquals("skos:notation", predicate.value());
        assertEquals("", predicate.namespace());
        assertFalse(predicate.optional());
        assertFalse(predicate.inverse());
    }

    @Test
    void shouldReadOptionalInversePredicate() throws NoSuchFieldException {
        var field = TestEntity.class.getDeclaredField("optionalInversePredicate");
        var predicate = field.getAnnotation(Predicate.class);

        assertNotNull(predicate);
        assertEquals("dcterms:hasPart", predicate.value());
        assertEquals("", predicate.namespace());
        assertTrue(predicate.optional());
        assertTrue(predicate.inverse());
    }

    @Test
    void shouldSupportAllCombinationsOfProperties() throws NoSuchFieldException {
        // Test que toutes les combinaisons de propriétés sont supportées
        var fields = TestEntity.class.getDeclaredFields();
        
        for (var field : fields) {
            var predicate = field.getAnnotation(Predicate.class);
            assertNotNull(predicate, "Field " + field.getName() + " should have @Predicate annotation");
            assertNotNull(predicate.value(), "Predicate value should not be null");
            assertNotNull(predicate.namespace(), "Predicate namespace should not be null");
        }
    }
}