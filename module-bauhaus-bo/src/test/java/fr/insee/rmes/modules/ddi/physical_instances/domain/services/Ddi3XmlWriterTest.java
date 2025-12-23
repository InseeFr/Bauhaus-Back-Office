package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

import static org.junit.jupiter.api.Assertions.*;

class Ddi3XmlWriterTest {

    private Ddi3XmlWriter writer;

    @BeforeEach
    void setUp() {
        writer = new Ddi3XmlWriter();
    }

    @Test
    void shouldWritePhysicalInstanceWithBasedOnObject() throws XMLStreamException {
        // Given
        BasedOnReference basedOnRef = new BasedOnReference(
                "fr.insee",
                "original-pi-id",
                "1",
                "PhysicalInstance"
        );
        BasedOnObject basedOnObject = new BasedOnObject(basedOnRef);

        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
                basedOnObject,
                new Citation(new Title(new StringValue("fr-FR", "Test Instance"))),
                new DataRelationshipReference("fr.insee", "dr-id", "1", "DataRelationship")
        );

        // When
        String xml = writer.buildPhysicalInstanceXml(pi);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<r:BasedOnObject>"));
        assertTrue(xml.contains("<r:BasedOnReference>"));
        assertTrue(xml.contains("<r:Agency>fr.insee</r:Agency>"));
        assertTrue(xml.contains("<r:ID>original-pi-id</r:ID>"));
        assertTrue(xml.contains("<r:Version>1</r:Version>"));
        assertTrue(xml.contains("<r:TypeOfObject>PhysicalInstance</r:TypeOfObject>"));
        assertTrue(xml.contains("</r:BasedOnReference>"));
        assertTrue(xml.contains("</r:BasedOnObject>"));
    }

    @Test
    void shouldWritePhysicalInstanceWithoutBasedOnObject() throws XMLStreamException {
        // Given
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "Test Instance"))),
                null
        );

        // When
        String xml = writer.buildPhysicalInstanceXml(pi);

        // Then
        assertNotNull(xml);
        assertFalse(xml.contains("<r:BasedOnObject>"));
        assertFalse(xml.contains("<r:BasedOnReference>"));
    }

    @Test
    void shouldWriteDataRelationshipWithBasedOnObject() throws XMLStreamException {
        // Given
        BasedOnReference basedOnRef = new BasedOnReference(
                "fr.insee",
                "original-dr-id",
                "1",
                "DataRelationship"
        );
        BasedOnObject basedOnObject = new BasedOnObject(basedOnRef);

        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-dr-id:1",
                "fr.insee",
                "new-dr-id",
                "1",
                basedOnObject,
                new DataRelationshipName(new StringValue("fr-FR", "Test DR")),
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<r:BasedOnObject>"));
        assertTrue(xml.contains("<r:BasedOnReference>"));
        assertTrue(xml.contains("<r:ID>original-dr-id</r:ID>"));
        assertTrue(xml.contains("<r:TypeOfObject>DataRelationship</r:TypeOfObject>"));
        assertTrue(xml.contains("</r:BasedOnReference>"));
        assertTrue(xml.contains("</r:BasedOnObject>"));
    }

    @Test
    void shouldWriteVariableWithBasedOnObject() throws XMLStreamException {
        // Given
        BasedOnReference basedOnRef = new BasedOnReference(
                "fr.insee",
                "original-var-id",
                "1",
                "Variable"
        );
        BasedOnObject basedOnObject = new BasedOnObject(basedOnRef);

        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-var-id:1",
                "fr.insee",
                "new-var-id",
                "1",
                basedOnObject,
                new VariableName(new StringValue("fr-FR", "TEST_VAR")),
                new Label(new Content("fr-FR", "Test Variable")),
                null,
                null,
                null
        );

        // When
        String xml = writer.buildVariableXml(var);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<r:BasedOnObject>"));
        assertTrue(xml.contains("<r:BasedOnReference>"));
        assertTrue(xml.contains("<r:ID>original-var-id</r:ID>"));
        assertTrue(xml.contains("<r:TypeOfObject>Variable</r:TypeOfObject>"));
        assertTrue(xml.contains("</r:BasedOnReference>"));
        assertTrue(xml.contains("</r:BasedOnObject>"));
    }

    @Test
    void shouldHandleBasedOnObjectWithNullBasedOnReference() throws XMLStreamException {
        // Given - BasedOnObject with null basedOnReference
        BasedOnObject basedOnObject = new BasedOnObject(null);

        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
                basedOnObject,
                new Citation(new Title(new StringValue("fr-FR", "Test Instance"))),
                null
        );

        // When
        String xml = writer.buildPhysicalInstanceXml(pi);

        // Then
        assertNotNull(xml);
        // Should not write BasedOnObject if basedOnReference is null
        assertFalse(xml.contains("<r:BasedOnObject>"));
    }
}
