package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Ddi3XmlWriterTest {

    private Ddi3XmlWriter writer;

    @BeforeEach
    void setUp() {
        writer = new Ddi3XmlWriter(Map.of(
            "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
            "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
            "Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
            "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
            "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"
        ));
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
                null,
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

    @Test
    void shouldWriteDataRelationshipWithLabel() throws XMLStreamException {
        // Given - DataRelationship with Label
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                new DataRelationshipName(new StringValue("fr-FR", "DR Name")),
                new Label(new Content("fr-FR", "DR Label")),
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<r:Label>"));
        assertTrue(xml.contains("<r:Content"));
        assertTrue(xml.contains("xml:lang=\"fr-FR\""));
        assertTrue(xml.contains("DR Label"));
        assertTrue(xml.contains("</r:Content>"));
        assertTrue(xml.contains("</r:Label>"));
    }

    @Test
    void shouldWriteDataRelationshipWithoutLabelWhenNull() throws XMLStreamException {
        // Given - DataRelationship without Label
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                new DataRelationshipName(new StringValue("fr-FR", "DR Name")),
                null,
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertNotNull(xml);
        // Should not contain Label element when label is null
        assertFalse(xml.contains("<r:Label>"));
    }

    @Test
    void shouldWriteLogicalRecordWithLabel() throws XMLStreamException {
        // Given - DataRelationship with LogicalRecord that has a Label
        LogicalRecord lr = new LogicalRecord(
                "true",
                "urn:ddi:fr.insee:lr-id:1",
                "fr.insee",
                "lr-id",
                "1",
                new LogicalRecordName(new StringValue("fr-FR", "LR Name")),
                new Label(new Content("fr-FR", "LR Label")),
                null
        );

        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                new DataRelationshipName(new StringValue("fr-FR", "DR Name")),
                null,
                lr
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<LogicalRecord"));
        assertTrue(xml.contains("LR Label"));
        // Verify the Label is inside LogicalRecord
        int logicalRecordStart = xml.indexOf("<LogicalRecord");
        int logicalRecordEnd = xml.indexOf("</LogicalRecord>");
        int labelIndex = xml.indexOf("LR Label");
        assertTrue(labelIndex > logicalRecordStart && labelIndex < logicalRecordEnd,
                "Label should be inside LogicalRecord element");
    }

    @Test
    void shouldWriteLogicalRecordWithoutLabelWhenNull() throws XMLStreamException {
        // Given - DataRelationship with LogicalRecord without Label
        LogicalRecord lr = new LogicalRecord(
                "true",
                "urn:ddi:fr.insee:lr-id:1",
                "fr.insee",
                "lr-id",
                "1",
                new LogicalRecordName(new StringValue("fr-FR", "LR Name")),
                null,
                null
        );

        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                new DataRelationshipName(new StringValue("fr-FR", "DR Name")),
                null,
                lr
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("<LogicalRecord"));
        // LogicalRecord should not have Label element
        int logicalRecordStart = xml.indexOf("<LogicalRecord");
        int logicalRecordEnd = xml.indexOf("</LogicalRecord>");
        String logicalRecordContent = xml.substring(logicalRecordStart, logicalRecordEnd);
        assertFalse(logicalRecordContent.contains("<r:Label>"),
                "LogicalRecord should not contain Label when label is null");
    }

    @Test
    void shouldHandleLabelWithNullContent() throws XMLStreamException {
        // Given - DataRelationship with Label that has null content (edge case)
        Label labelWithNullContent = new Label(null);

        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                new DataRelationshipName(new StringValue("fr-FR", "DR Name")),
                labelWithNullContent,
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then - Should not throw NPE and should not contain Label element
        assertNotNull(xml);
        assertFalse(xml.contains("<r:Label>"),
                "Should not write Label element when content is null");
    }
}
