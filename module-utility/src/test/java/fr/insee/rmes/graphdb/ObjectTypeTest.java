package fr.insee.rmes.graphdb;

import fr.insee.rmes.Constants;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ObjectTypeTest {

    @Test
    void shouldGetEnumByLabel() {
        Optional<ObjectType> concept = ObjectType.getEnumByLabel(Constants.CONCEPT);
        assertTrue(concept.isPresent());
        assertEquals(ObjectType.CONCEPT, concept.get());

        Optional<ObjectType> collection = ObjectType.getEnumByLabel(Constants.COLLECTION);
        assertTrue(collection.isPresent());
        assertEquals(ObjectType.COLLECTION, collection.get());

        Optional<ObjectType> family = ObjectType.getEnumByLabel(Constants.FAMILY);
        assertTrue(family.isPresent());
        assertEquals(ObjectType.FAMILY, family.get());
    }

    @Test
    void shouldReturnEmptyOptionalForUnknownLabel() {
        Optional<ObjectType> unknown = ObjectType.getEnumByLabel("unknown");
        assertFalse(unknown.isPresent());
    }

    @Test
    void shouldGetEnumByUri() {
        assertEquals(ObjectType.CONCEPT, ObjectType.getEnum(SKOS.CONCEPT));
        assertEquals(ObjectType.COLLECTION, ObjectType.getEnum(SKOS.COLLECTION));
        assertEquals(ObjectType.FAMILY, ObjectType.getEnum(INSEE.FAMILY));
        assertEquals(ObjectType.ORGANIZATION, ObjectType.getEnum(ORG.ORGANIZATION));
        assertEquals(ObjectType.DOCUMENT, ObjectType.getEnum(FOAF.DOCUMENT));
        assertEquals(ObjectType.DATASET, ObjectType.getEnum(DCAT.DATASET));
        assertEquals(ObjectType.DISTRIBUTION, ObjectType.getEnum(DCAT.DISTRIBUTION));
    }

    @Test
    void shouldReturnUndefinedForUnknownUri() {
        assertEquals(ObjectType.UNDEFINED, ObjectType.getEnum(null));
    }

    @Test
    void shouldGetLabelTypeByUri() {
        assertEquals(Constants.CONCEPT, ObjectType.getLabelType(SKOS.CONCEPT));
        assertEquals(Constants.COLLECTION, ObjectType.getLabelType(SKOS.COLLECTION));
        assertEquals(Constants.FAMILY, ObjectType.getLabelType(INSEE.FAMILY));
        assertEquals("organization", ObjectType.getLabelType(ORG.ORGANIZATION));
        assertEquals(Constants.DOCUMENT, ObjectType.getLabelType(FOAF.DOCUMENT));
    }

    @Test
    void shouldReturnUndefinedLabelForUnknownUri() {
        assertEquals(Constants.UNDEFINED, ObjectType.getLabelType(null));
    }

    @Test
    void shouldHaveCorrectLabelTypes() {
        assertEquals(Constants.CONCEPT, ObjectType.CONCEPT.labelType());
        assertEquals(Constants.COLLECTION, ObjectType.COLLECTION.labelType());
        assertEquals(Constants.FAMILY, ObjectType.FAMILY.labelType());
        assertEquals("series", ObjectType.SERIES.labelType());
        assertEquals("operation", ObjectType.OPERATION.labelType());
        assertEquals("indicator", ObjectType.INDICATOR.labelType());
        assertEquals("documentation", ObjectType.DOCUMENTATION.labelType());
        assertEquals(Constants.DOCUMENT, ObjectType.DOCUMENT.labelType());
        assertEquals("link", ObjectType.LINK.labelType());
        assertEquals("organization", ObjectType.ORGANIZATION.labelType());
        assertEquals("structure", ObjectType.STRUCTURE.labelType());
        assertEquals(Constants.CODELIST, ObjectType.CODE_LIST.labelType());
        assertEquals(Constants.DATASET, ObjectType.DATASET.labelType());
        assertEquals(Constants.DISTRIBUTION, ObjectType.DISTRIBUTION.labelType());
        assertEquals("measureProperty", ObjectType.MEASURE_PROPERTY.labelType());
        assertEquals("attributeProperty", ObjectType.ATTRIBUTE_PROPERTY.labelType());
        assertEquals("dimensionProperty", ObjectType.DIMENSION_PROPERTY.labelType());
        assertEquals(Constants.UNDEFINED, ObjectType.UNDEFINED.labelType());
    }

    @Test
    void shouldHaveCorrectBaseUriPropertyNames() {
        assertNotNull(ObjectType.CONCEPT.baseUriPropertyName());
        assertNotNull(ObjectType.COLLECTION.baseUriPropertyName());
        assertNotNull(ObjectType.FAMILY.baseUriPropertyName());
        assertNull(ObjectType.ORGANIZATION.baseUriPropertyName());
        assertNull(ObjectType.UNDEFINED.baseUriPropertyName());
    }

    @Test
    void shouldHaveCorrectBaseUriModifiers() {
        assertNotNull(ObjectType.CONCEPT.baseUriModifier());
        assertNotNull(ObjectType.COLLECTION.baseUriModifier());
        assertNotNull(ObjectType.FAMILY.baseUriModifier());
        
        // Test identity modifier
        assertEquals("test", ObjectType.CONCEPT.baseUriModifier().apply("test"));
        assertEquals("test", ObjectType.COLLECTION.baseUriModifier().apply("test"));
        
        // Test specific modifiers
        assertEquals("testmesure", ObjectType.MEASURE_PROPERTY.baseUriModifier().apply("test"));
        assertEquals("testattribut", ObjectType.ATTRIBUTE_PROPERTY.baseUriModifier().apply("test"));
        assertEquals("testdimension", ObjectType.DIMENSION_PROPERTY.baseUriModifier().apply("test"));
        assertEquals("", ObjectType.ORGANIZATION.baseUriModifier().apply("test"));
        assertEquals("", ObjectType.UNDEFINED.baseUriModifier().apply("test"));
    }

    @Test
    void shouldHaveAllRequiredEnumValues() {
        ObjectType[] values = ObjectType.values();
        assertTrue(values.length >= 18); // At least 18 types defined
        
        // Verify key types exist
        boolean hasConceptType = false;
        boolean hasCollectionType = false;
        boolean hasFamilyType = false;
        boolean hasUndefinedType = false;
        
        for (ObjectType type : values) {
            if (type == ObjectType.CONCEPT) hasConceptType = true;
            if (type == ObjectType.COLLECTION) hasCollectionType = true;
            if (type == ObjectType.FAMILY) hasFamilyType = true;
            if (type == ObjectType.UNDEFINED) hasUndefinedType = true;
        }
        
        assertTrue(hasConceptType);
        assertTrue(hasCollectionType);
        assertTrue(hasFamilyType);
        assertTrue(hasUndefinedType);
    }
}