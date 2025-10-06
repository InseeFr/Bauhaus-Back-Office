package fr.insee.rmes.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.annotations.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SparqlQueryBuilderTest {

    @Mock
    private Environment mockEnvironment;

    @Entity(type = "skos:ConceptScheme")
    @Graph("${test.graph}")
    public record TestEntity(
            @Statement
            String uri,
            
            @Predicate(value = "skos:notation")
            String id,
            
            @Predicate(value = "skos:prefLabel", namespace = "http://www.w3.org/2004/02/skos/core#", lang = "lg1")
            String labelLg1,
            
            @Predicate(value = "rdfs:comment")
            String description,
            
            @Predicate(value = "skos:altLabel", optional = true)
            String optionalField,
            
            @Predicate(value = "dcterms:title", optional = false)
            String mandatoryField,
            
            @Predicate(value = "rdfs:seeAlso", inverse = true)
            String inverseField,
            
            @Predicate(value = "skos:broader", optional = false, inverse = true)
            String mandatoryInverseField
    ) {}

    @Entity(type = "foaf:Person")
    public record SimpleEntity(
            @Statement
            String uri,
            
            @Predicate(value = "foaf:name")
            String name
    ) {}

    @Entity
    public record EntityWithoutType(
            @Statement
            String uri,
            
            @Predicate(value = "rdfs:label")
            String label
    ) {}


    @Test
    void shouldCreateQueryBuilderForEntity() {
        SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
        assertNotNull(builder);
    }

    @Test
    void shouldCreateQueryBuilderWithEnvironment() {
        SparqlQueryBuilder<TestEntity> builder = new SparqlQueryBuilder<>(TestEntity.class, mockEnvironment);
        assertNotNull(builder);
    }

    @Test
    void shouldSelectSpecificFields() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("id", "labelLg1").build();

            assertNotNull(query);
            assertTrue(query.contains("SELECT ?id ?labelLg1"));
            assertTrue(query.contains("FROM <http://test.graph>"));
            assertTrue(query.contains("?testentity rdf:type skos:ConceptScheme"));
            assertTrue(query.contains("?testentity skos:notation ?id"));
        }
    }

    @Test
    void shouldSelectAllFields() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.selectAll().build();

            assertNotNull(query);
            assertTrue(query.contains("SELECT"));
            assertTrue(query.contains("?uri"));
            assertTrue(query.contains("?id"));
            assertTrue(query.contains("?labelLg1"));
            assertTrue(query.contains("?description"));
        }
    }

    @Test
    void shouldAddWhereCondition() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("id", "labelLg1")
                    .where("id", "TEST001")
                    .build();

            assertNotNull(query);
            assertTrue(query.contains("?testentity skos:notation ?id"));
            assertTrue(query.contains("FILTER(?id = \"TEST001\")"));
        }
    }

    @Test
    void shouldAddUriWhereCondition() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("id")
                    .where("uri", "http://example.com/resource")
                    .build();

            assertNotNull(query);
            assertTrue(query.contains("BIND(<http://example.com/resource> AS ?uri)"));
        }
    }

    @Test
    void shouldThrowExceptionForInvalidField() {
        SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> builder.where("invalidField", "value"));
        
        assertTrue(exception.getMessage().contains("Field 'invalidField' not found"));
    }

    @Test
    void shouldAddOrderBy() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("id", "labelLg1")
                    .orderBy("labelLg1", "ASC")
                    .build();

            assertNotNull(query);
            assertTrue(query.contains("ORDER BY ASC(?labelLg1)"));
        }
    }

    @Test
    void shouldAddLimit() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("id")
                    .limit(10)
                    .build();

            assertNotNull(query);
            assertTrue(query.contains("LIMIT 10"));
        }
    }

    @Test
    void shouldAddOffset() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("id")
                    .offset(5)
                    .build();

            assertNotNull(query);
            assertTrue(query.contains("OFFSET 5"));
        }
    }

    @Test
    void shouldAddDistinct() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("id")
                    .distinct()
                    .build();

            assertNotNull(query);
            assertTrue(query.contains("SELECT DISTINCT ?id"));
        }
    }

    @Test
    void shouldHandleEntityWithoutGraph() throws RmesException {
        SparqlQueryBuilder<SimpleEntity> builder = SparqlQueryBuilder.forEntity(SimpleEntity.class);
        String query = builder.select("name").build();

        assertNotNull(query);
        assertFalse(query.contains("FROM"));
        assertTrue(query.contains("?simpleentity rdf:type foaf:Person"));
    }

    @Test
    void shouldHandleEntityWithoutType() throws RmesException {
        SparqlQueryBuilder<EntityWithoutType> builder = SparqlQueryBuilder.forEntity(EntityWithoutType.class);
        String query = builder.select("label").build();
        
        System.out.println("Generated query for EntityWithoutType:\n" + query);
        
        assertNotNull(query);
        // The key test: no rdf:type statement should be present since EntityWithoutType has no type
        assertFalse(query.contains(" rdf:type "), "Query should not contain rdf:type statement");
        // But it should still contain the label predicate
        assertTrue(query.contains("rdfs:label"), "Query should contain rdfs:label predicate");
        assertTrue(query.contains("entitywithouttype"), "Query should reference the entity");
    }

    @Test
    void shouldGeneratePrefixesFromNamespaces() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("labelLg1").build();

            assertNotNull(query);
            assertTrue(query.contains("PREFIX skos:<http://www.w3.org/2004/02/skos/core#>"));
        }
    }

    @Test
    void shouldHandleLanguageLabels() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${fr.insee.rmes.bauhaus.lg1}"))
                    .thenReturn("fr");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("labelLg1").build();

            assertNotNull(query);
            assertTrue(query.contains("FILTER(lang(?labelLg1) = \"fr\")"));
        }
    }

    @Test
    void shouldHandleStatementFields() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("uri").build();

            assertNotNull(query);
            assertTrue(query.contains("BIND(?testentity AS ?uri)"));
        }
    }

    @Test
    void shouldReturnAvailableFields() {
        SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
        var fields = builder.getAvailableFields();

        assertNotNull(fields);
        assertTrue(fields.contains("uri"));
        assertTrue(fields.contains("id"));
        assertTrue(fields.contains("labelLg1"));
        assertTrue(fields.contains("description"));
    }

    @Test
    void shouldReturnFieldToPredicateMapping() {
        SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
        var mapping = builder.getFieldToPredicateMapping();

        assertNotNull(mapping);
        assertEquals("URI", mapping.get("uri"));
        assertEquals("skos:notation", mapping.get("id"));
        assertEquals("skos:prefLabel", mapping.get("labelLg1"));
        assertEquals("rdfs:comment", mapping.get("description"));
    }

    @Test
    void shouldBuildComplexQuery() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder
                    .select("id", "labelLg1", "description")
                    .where("id", "TEST001")
                    .orderBy("labelLg1", "ASC")
                    .limit(10)
                    .offset(5)
                    .distinct()
                    .build();

            assertNotNull(query);
            assertTrue(query.contains("SELECT DISTINCT ?id ?labelLg1 ?description"));
            assertTrue(query.contains("FROM <http://test.graph>"));
            assertTrue(query.contains("?testentity rdf:type skos:ConceptScheme"));
            assertTrue(query.contains("FILTER(?id = \"TEST001\")"));
            assertTrue(query.contains("ORDER BY ASC(?labelLg1)"));
            assertTrue(query.contains("OFFSET 5"));
            assertTrue(query.contains("LIMIT 10"));
        }
    }

    @Test
    void shouldHandleHttpPredicateUris() throws RmesException {
        @Entity(type = "test:Entity")
        record EntityWithHttpUris(
                @Predicate(value = "http://example.com/property")
                String field
        ) {}

        SparqlQueryBuilder<EntityWithHttpUris> builder = SparqlQueryBuilder.forEntity(EntityWithHttpUris.class);
        String query = builder.select("field").build();

        assertNotNull(query);
        assertTrue(query.contains("<http://example.com/property>"));
    }

    @Test
    void shouldGenerateOptionalTripleForOptionalField() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("optionalField").build();

            assertNotNull(query);
            assertTrue(query.contains("OPTIONAL { ?testentity skos:altLabel ?optionalField"));
        }
    }

    @Test
    void shouldGenerateMandatoryTripleForMandatoryField() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("mandatoryField").build();

            assertNotNull(query);
            assertTrue(query.contains("?testentity dcterms:title ?mandatoryField ."));
            assertFalse(query.contains("OPTIONAL { ?testentity dcterms:title ?mandatoryField"));
        }
    }

    @Test
    void shouldGenerateInverseTripleForInverseField() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("inverseField").build();

            assertNotNull(query);
            assertTrue(query.contains("?inverseField rdfs:seeAlso ?testentity"));
        }
    }

    @Test
    void shouldGenerateMandatoryInverseTripleForMandatoryInverseField() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("mandatoryInverseField").build();

            assertNotNull(query);
            assertTrue(query.contains("?mandatoryInverseField skos:broader ?testentity ."));
            assertFalse(query.contains("OPTIONAL"));
        }
    }

    @Test
    void shouldHandleComplexQueryWithOptionalAndInverseFields() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("optionalField", "mandatoryField", "inverseField", "mandatoryInverseField").build();

            assertNotNull(query);
            // Optional field should be in OPTIONAL block
            assertTrue(query.contains("OPTIONAL { ?testentity skos:altLabel ?optionalField"));
            // Mandatory field should NOT be in OPTIONAL block
            assertTrue(query.contains("?testentity dcterms:title ?mandatoryField ."));
            // Inverse field should be in OPTIONAL block with inverted subject/object
            assertFalse(query.contains("OPTIONAL { ?inverseField rdfs:seeAlso ?testentity"));
            // Mandatory inverse field should NOT be in OPTIONAL block but with inverted subject/object
            assertTrue(query.contains("?mandatoryInverseField skos:broader ?testentity ."));
        }
    }

    @Test
    void shouldReturnCorrectFieldMappingsWithOptionalAndInverse() {
        SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
        var mapping = builder.getFieldToPredicateMapping();

        assertNotNull(mapping);
        assertEquals("skos:altLabel", mapping.get("optionalField"));
        assertEquals("dcterms:title", mapping.get("mandatoryField"));
        assertEquals("rdfs:seeAlso", mapping.get("inverseField"));
        assertEquals("skos:broader", mapping.get("mandatoryInverseField"));
    }

    @Test
    void shouldDefaultToOptionalFalseWhenNotSpecified() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("description").build();

            assertNotNull(query);
            // Default behavior should be optional=true
            assertFalse(query.contains("OPTIONAL { ?testentity rdfs:comment ?description"));
        }
    }

    @Test
    void shouldDefaultToInverseFalseWhenNotSpecified() throws RmesException {
        try (MockedStatic<PropertyResolver> mockedPropertyResolver = mockStatic(PropertyResolver.class)) {
            mockedPropertyResolver.when(() -> PropertyResolver.resolve("${test.graph}"))
                    .thenReturn("http://test.graph");

            SparqlQueryBuilder<TestEntity> builder = SparqlQueryBuilder.forEntity(TestEntity.class);
            String query = builder.select("description").build();

            assertNotNull(query);
            // Default behavior should be inverse=false (normal subject-predicate-object order)
            assertTrue(query.contains("?testentity rdfs:comment ?description"));
            assertFalse(query.contains("?description rdfs:comment ?testentity"));
        }
    }
}