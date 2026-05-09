package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QBTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://purl.org/linked-data/cube#", QB.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("qb", QB.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = QB.NS;
        assertNotNull(ns);
        assertEquals("qb", ns.getPrefix());
        assertEquals("http://purl.org/linked-data/cube#", ns.getName());
    }

    @Test
    void shouldHaveValidDataStructureDefinition() {
        assertNotNull(QB.DATA_STRUCTURE_DEFINITION);
        assertEquals("http://purl.org/linked-data/cube#DataStructureDefinition", QB.DATA_STRUCTURE_DEFINITION.toString());
    }

    @Test
    void shouldHaveValidComponentConstants() {
        assertNotNull(QB.COMPONENT);
        assertEquals("http://purl.org/linked-data/cube#component", QB.COMPONENT.toString());

        assertNotNull(QB.COMPONENT_REQUIRED);
        assertEquals("http://purl.org/linked-data/cube#componentRequired", QB.COMPONENT_REQUIRED.toString());

        assertNotNull(QB.COMPONENT_SPECIFICATION);
        assertEquals("http://purl.org/linked-data/cube#ComponentSpecification", QB.COMPONENT_SPECIFICATION.toString());

        assertNotNull(QB.COMPONENT_ATTACHMENT);
        assertEquals("http://purl.org/linked-data/cube#componentAttachment", QB.COMPONENT_ATTACHMENT.toString());
    }

    @Test
    void shouldHaveValidMeasureConstants() {
        assertNotNull(QB.MEASURE);
        assertEquals("http://purl.org/linked-data/cube#measure", QB.MEASURE.toString());

        assertNotNull(QB.MEASURE_PROPERTY);
        assertEquals("http://purl.org/linked-data/cube#MeasureProperty", QB.MEASURE_PROPERTY.toString());
    }

    @Test
    void shouldHaveValidAttributeConstants() {
        assertNotNull(QB.ATTRIBUTE);
        assertEquals("http://purl.org/linked-data/cube#attribute", QB.ATTRIBUTE.toString());

        assertNotNull(QB.ATTRIBUTE_PROPERTY);
        assertEquals("http://purl.org/linked-data/cube#AttributeProperty", QB.ATTRIBUTE_PROPERTY.toString());
    }

    @Test
    void shouldHaveValidDimensionConstants() {
        assertNotNull(QB.DIMENSION);
        assertEquals("http://purl.org/linked-data/cube#dimension", QB.DIMENSION.toString());

        assertNotNull(QB.DIMENSION_PROPERTY);
        assertEquals("http://purl.org/linked-data/cube#DimensionProperty", QB.DIMENSION_PROPERTY.toString());
    }

    @Test
    void shouldHaveValidCodeListConstants() {
        assertNotNull(QB.CODE_LIST);
        assertEquals("http://purl.org/linked-data/cube#codeList", QB.CODE_LIST.toString());

        assertNotNull(QB.CODED_PROPERTY);
        assertEquals("http://purl.org/linked-data/cube#CodedProperty", QB.CODED_PROPERTY.toString());
    }

    @Test
    void shouldHaveValidConceptAndOrderConstants() {
        assertNotNull(QB.CONCEPT);
        assertEquals("http://purl.org/linked-data/cube#concept", QB.CONCEPT.toString());

        assertNotNull(QB.ORDER);
        assertEquals("http://purl.org/linked-data/cube#order", QB.ORDER.toString());
    }

    @Test
    void shouldReturnCorrectURIForComponent() {
        String[] uris = QB.getURIForComponent();
        assertNotNull(uris);
        assertEquals(3, uris.length);
        
        assertTrue(uris[0].contains("MeasureProperty"));
        assertTrue(uris[1].contains("AttributeProperty"));
        assertTrue(uris[2].contains("DimensionProperty"));
    }

}