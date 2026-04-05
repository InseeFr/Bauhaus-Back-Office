package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:PhysicalInstance isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:physicalinstance:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:new-pi-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">new-pi-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><r:BasedOnObject xmlns:r=\"ddi:reusable:3_3\"><r:BasedOnReference><r:Agency>fr.insee</r:Agency><r:ID>original-pi-id</r:ID><r:Version>1</r:Version><r:TypeOfObject>PhysicalInstance</r:TypeOfObject></r:BasedOnReference></r:BasedOnObject><r:Citation xmlns:r=\"ddi:reusable:3_3\"><r:Title><r:String xml:lang=\"fr-FR\">Test Instance</r:String></r:Title></r:Citation><r:DataRelationshipReference xmlns:r=\"ddi:reusable:3_3\"><r:Agency>fr.insee</r:Agency><r:ID>dr-id</r:ID><r:Version>1</r:Version><r:TypeOfObject>DataRelationship</r:TypeOfObject></r:DataRelationshipReference></ddi:PhysicalInstance></Fragment>", xml);
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
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:PhysicalInstance isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:physicalinstance:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:new-pi-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">new-pi-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><r:Citation xmlns:r=\"ddi:reusable:3_3\"><r:Title><r:String xml:lang=\"fr-FR\">Test Instance</r:String></r:Title></r:Citation></ddi:PhysicalInstance></Fragment>", xml);
    }

    @Test
    void shouldHandleBasedOnObjectWithNullBasedOnReference() throws XMLStreamException {
        // Given
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
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:PhysicalInstance isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:physicalinstance:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:new-pi-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">new-pi-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><r:Citation xmlns:r=\"ddi:reusable:3_3\"><r:Title><r:String xml:lang=\"fr-FR\">Test Instance</r:String></r:Title></r:Citation></ddi:PhysicalInstance></Fragment>", xml);
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
                null,
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:logicalproduct:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:new-dr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">new-dr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><r:BasedOnObject xmlns:r=\"ddi:reusable:3_3\"><r:BasedOnReference><r:Agency>fr.insee</r:Agency><r:ID>original-dr-id</r:ID><r:Version>1</r:Version><r:TypeOfObject>DataRelationship</r:TypeOfObject></r:BasedOnReference></r:BasedOnObject></ddi:DataRelationship></Fragment>", xml);
    }

    @Test
    void shouldWriteDataRelationshipWithLabel() throws XMLStreamException {
        // Given
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                new Label(new Content("fr-FR", "DR Label")),
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:logicalproduct:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:dr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">dr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><ddi:DataRelationshipName><r:String xml:lang=\"fr-FR\" xmlns:r=\"ddi:reusable:3_3\">DR Label</r:String></ddi:DataRelationshipName><r:Label xmlns:r=\"ddi:reusable:3_3\"><r:Content xml:lang=\"fr-FR\">DR Label</r:Content></r:Label></ddi:DataRelationship></Fragment>", xml);
    }

    @Test
    void shouldWriteDataRelationshipWithoutLabelWhenNull() throws XMLStreamException {
        // Given
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                null,
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:logicalproduct:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:dr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">dr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version></ddi:DataRelationship></Fragment>", xml);
    }

    @Test
    void shouldHandleLabelWithNullContent() throws XMLStreamException {
        // Given
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                new Label(null),
                null
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:logicalproduct:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:dr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">dr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version></ddi:DataRelationship></Fragment>", xml);
    }

    @Test
    void shouldWriteLogicalRecordWithLabel() throws XMLStreamException {
        // Given
        LogicalRecord lr = new LogicalRecord(
                "true",
                "urn:ddi:fr.insee:lr-id:1",
                "fr.insee",
                "lr-id",
                "1",
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
                null,
                lr
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:logicalproduct:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:dr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">dr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><ddi:LogicalRecord isUniversallyUnique=\"true\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:lr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">lr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><ddi:LogicalRecordName><r:String xml:lang=\"fr-FR\" xmlns:r=\"ddi:reusable:3_3\">LR Label</r:String></ddi:LogicalRecordName><r:Label xmlns:r=\"ddi:reusable:3_3\"><r:Content xml:lang=\"fr-FR\">LR Label</r:Content></r:Label></ddi:LogicalRecord></ddi:DataRelationship></Fragment>", xml);
    }

    @Test
    void shouldWriteLogicalRecordWithoutLabelWhenNull() throws XMLStreamException {
        // Given
        LogicalRecord lr = new LogicalRecord(
                "true",
                "urn:ddi:fr.insee:lr-id:1",
                "fr.insee",
                "lr-id",
                "1",
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
                null,
                lr
        );

        // When
        String xml = writer.buildDataRelationshipXml(dr);

        // Then
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:logicalproduct:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:dr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">dr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><ddi:LogicalRecord isUniversallyUnique=\"true\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:lr-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">lr-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version></ddi:LogicalRecord></ddi:DataRelationship></Fragment>", xml);
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
        assertEquals("<Fragment xmlns=\"ddi:instance:3_3\"><ddi:Variable isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:logicalproduct:3_3\"><r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:new-var-id:1</r:URN><r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency><r:ID xmlns:r=\"ddi:reusable:3_3\">new-var-id</r:ID><r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version><r:BasedOnObject xmlns:r=\"ddi:reusable:3_3\"><r:BasedOnReference><r:Agency>fr.insee</r:Agency><r:ID>original-var-id</r:ID><r:Version>1</r:Version><r:TypeOfObject>Variable</r:TypeOfObject></r:BasedOnReference></r:BasedOnObject><ddi:VariableName><r:String xml:lang=\"fr-FR\" xmlns:r=\"ddi:reusable:3_3\">TEST_VAR</r:String></ddi:VariableName><r:Label xmlns:r=\"ddi:reusable:3_3\"><r:Content xml:lang=\"fr-FR\">Test Variable</r:Content></r:Label><ddi:VariableRepresentation/></ddi:Variable></Fragment>", xml);
    }

    @Test
    void shouldWriteGroupXml() throws XMLStreamException {
        // Given
        Ddi4Group group = new Ddi4Group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(new Title(new StringValue("fr-FR", "Enquête innovation Group"))),
                List.of(new StudyUnitReference("fr.insee", "su-id-1", "1", "StudyUnit")),
                "http://id.insee.fr/operations/serie/s1001",
                "insee:StatisticalOperationSeries"
        );

        // When
        String xml = writer.buildGroupXml(group);

        // Then
        assertThat(xml).contains("<Fragment");
        assertThat(xml).contains("isUniversallyUnique=\"true\"");
        assertThat(xml).contains("versionDate=\"2026-04-03T12:00:00Z\"");
        assertThat(xml).contains(">urn:ddi:fr.insee:group-id:1<");
        assertThat(xml).contains(">fr.insee<");
        assertThat(xml).contains(">group-id<");
        assertThat(xml).contains(">1<");
        assertThat(xml).contains("typeOfUserID=\"URI\"");
        assertThat(xml).contains(">http://id.insee.fr/operations/serie/s1001<");
        assertThat(xml).contains(">insee:StatisticalOperationSeries<");
        assertThat(xml).contains("xml:lang=\"fr-FR\"");
        assertThat(xml).contains(">Enquête innovation Group<");
        assertThat(xml).contains(">su-id-1<");
        assertThat(xml).contains(">StudyUnit<");
    }

    @Test
    void shouldWriteGroupXmlWithoutOptionalFields() throws XMLStreamException {
        // Given
        Ddi4Group group = new Ddi4Group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(new Title(new StringValue("fr-FR", "Test Group"))),
                List.of(),
                null, null
        );

        // When
        String xml = writer.buildGroupXml(group);

        // Then
        assertThat(xml).contains("<Fragment");
        assertThat(xml).contains(">Test Group<");
        assertThat(xml).doesNotContain("typeOfUserID");
        assertThat(xml).doesNotContain("TypeOfGroup");
    }

    @Test
    void shouldWriteStudyUnitXml() throws XMLStreamException {
        // Given
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(new Title(new StringValue("fr-FR", "BPE 2021 StudyUnit"))),
                "http://id.insee.fr/operations/operation/s1001a1"
        );

        // When
        String xml = writer.buildStudyUnitXml(studyUnit);

        // Then
        assertThat(xml).contains("<Fragment");
        assertThat(xml).contains("isUniversallyUnique=\"true\"");
        assertThat(xml).contains("versionDate=\"2026-04-03T12:00:00Z\"");
        assertThat(xml).contains(">urn:ddi:fr.insee:su-id:1<");
        assertThat(xml).contains(">fr.insee<");
        assertThat(xml).contains(">su-id<");
        assertThat(xml).contains("typeOfUserID=\"URI\"");
        assertThat(xml).contains(">http://id.insee.fr/operations/operation/s1001a1<");
        assertThat(xml).contains("xml:lang=\"fr-FR\"");
        assertThat(xml).contains(">BPE 2021 StudyUnit<");
    }

    @Test
    void shouldWriteStudyUnitXmlWithoutOperationIri() throws XMLStreamException {
        // Given
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(new Title(new StringValue("fr-FR", "Test SU"))),
                null
        );

        // When
        String xml = writer.buildStudyUnitXml(studyUnit);

        // Then
        assertThat(xml).contains("<Fragment");
        assertThat(xml).contains(">Test SU<");
        assertThat(xml).doesNotContain("typeOfUserID");
    }
}