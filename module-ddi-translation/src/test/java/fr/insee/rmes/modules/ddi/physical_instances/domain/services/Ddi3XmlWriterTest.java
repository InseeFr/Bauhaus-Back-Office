package fr.insee.rmes.modules.ddi.physical_instances.domain.services;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Group;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.generated.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Variable;
import fr.insee.rmes.modules.ddi.physical_instances.generated.DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.generated.PhysicalInstance;
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

        PhysicalInstance pi = physicalInstance(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
                basedOnObject,
                new Citation(LangStrings.of("fr-FR", "Test Instance")),
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
        PhysicalInstance pi = physicalInstance(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Test Instance")),
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

        PhysicalInstance pi = physicalInstance(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
                basedOnObject,
                new Citation(LangStrings.of("fr-FR", "Test Instance")),
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

        DataRelationship dr = dataRelationship(
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
        DataRelationship dr = dataRelationship(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                LangStrings.of("fr-FR", "DR Label"),
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
        DataRelationship dr = dataRelationship(
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
        DataRelationship dr = dataRelationship(
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
    void shouldWriteLogicalRecordWithLabel() throws XMLStreamException {
        // Given
        LogicalRecord lr = new LogicalRecord(
                "true",
                "urn:ddi:fr.insee:lr-id:1",
                "fr.insee",
                "lr-id",
                "1",
                LangStrings.of("fr-FR", "LR Label"),
                null
        );

        DataRelationship dr = dataRelationship(
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

        DataRelationship dr = dataRelationship(
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

        Variable var = variable(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:new-var-id:1",
                "fr.insee",
                "new-var-id",
                "1",
                basedOnObject,
                LangStrings.of("fr-FR", "TEST_VAR"),
                LangStrings.of("fr-FR", "Test Variable"),
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
    void shouldEmitOnlyCodeListReferenceForCodeRepresentation() throws XMLStreamException {
        Variable var = variable(
                "true",
                "2025-12-23T09:52:06.355Z",
                "urn:ddi:fr.insee:var-coderep:1",
                "fr.insee",
                "var-coderep",
                "1",
                null,
                LangStrings.of("fr-FR", "VAR_CODEREP"),
                LangStrings.of("fr-FR", "Variable avec CodeRepresentation"),
                null,
                new VariableRepresentation(
                        null,
                        new CodeRepresentation(
                                "true",
                                new CodeListReference("fr.insee", "fc65a527-a04b-4505-85de-0a181e54dbad", "1", "CodeList")
                        ),
                        null,
                        null,
                        null
                ),
                null
        );

        String xml = writer.buildVariableXml(var);

        assertThat(xml)
                .contains("<r:CodeListReference")
                .contains(">fc65a527-a04b-4505-85de-0a181e54dbad<")
                .contains(">CodeList<")
                .doesNotContain("<l:CodeList")
                .doesNotContain("<ddi:CodeList");
    }

    @Test
    void shouldWriteGroupXml() throws XMLStreamException {
        // Given
        Group group = group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(LangStrings.of("fr-FR", "Enquête innovation Group")),
                List.of(new StudyUnitReference("fr.insee", "su-id-1", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
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
        Group group = group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(LangStrings.of("fr-FR", "Test Group")),
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
        StudyUnit studyUnit = studyUnit(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(LangStrings.of("fr-FR", "BPE 2021 StudyUnit")),
                "http://id.insee.fr/operations/operation/s1001a1",
                null
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
        StudyUnit studyUnit = studyUnit(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(LangStrings.of("fr-FR", "Test SU")),
                null,
                null
        );

        // When
        String xml = writer.buildStudyUnitXml(studyUnit);

        // Then
        assertThat(xml).contains("<Fragment");
        assertThat(xml).contains(">Test SU<");
        assertThat(xml).doesNotContain("typeOfUserID");
    }
    /** Construit une Variable generee equivalente a l'ancien record Ddi4Variable (memes arguments). */
    private static Variable variable(String isUniversallyUnique, String versionDate, String urn, String agency,
                                     String id, String version, BasedOnObject basedOnObject,
                                     List<LangString> variableName, List<LangString> label,
                                     List<LangString> description, VariableRepresentation representation,
                                     String isGeographic) {
        Variable variable = new Variable();
        variable.setURN(urn);
        variable.setAgency(agency);
        variable.setID(id);
        variable.setVersion(version);
        variable.setVariableName(variableName);
        variable.setLabel(label);
        variable.setDescription(description);
        variable.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        variable.putAdditionalProperty("@versionDate", versionDate);
        variable.putAdditionalProperty("@isGeographic", isGeographic);
        if (basedOnObject != null) {
            variable.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        if (representation != null) {
            variable.putAdditionalProperty("VariableRepresentation", representation);
        }
        return variable;
    }
    /** Construit une DataRelationship generee equivalente a l'ancien record (memes arguments). */
    private static DataRelationship dataRelationship(String isUniversallyUnique, String versionDate, String urn,
                                                     String agency, String id, String version,
                                                     BasedOnObject basedOnObject, List<LangString> label,
                                                     LogicalRecord logicalRecord) {
        DataRelationship dataRelationship = new DataRelationship();
        dataRelationship.setURN(urn);
        dataRelationship.setAgency(agency);
        dataRelationship.setID(id);
        dataRelationship.setVersion(version);
        dataRelationship.setLabel(label);
        dataRelationship.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        dataRelationship.putAdditionalProperty("@versionDate", versionDate);
        if (basedOnObject != null) {
            dataRelationship.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        if (logicalRecord != null) {
            dataRelationship.putAdditionalProperty("LogicalRecord", logicalRecord);
        }
        return dataRelationship;
    }
    /** Construit une PhysicalInstance generee equivalente a l'ancien record (memes arguments). */
    private static PhysicalInstance physicalInstance(String isUniversallyUnique, String versionDate, String urn,
                                                     String agency, String id, String version,
                                                     BasedOnObject basedOnObject, Citation citation,
                                                     DataRelationshipReference dataRelationshipReference) {
        PhysicalInstance physicalInstance = new PhysicalInstance();
        physicalInstance.setURN(urn);
        physicalInstance.setAgency(agency);
        physicalInstance.setID(id);
        physicalInstance.setVersion(version);
        physicalInstance.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        physicalInstance.putAdditionalProperty("@versionDate", versionDate);
        if (basedOnObject != null) {
            physicalInstance.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        if (citation != null) {
            physicalInstance.putAdditionalProperty("Citation", citation);
        }
        if (dataRelationshipReference != null) {
            physicalInstance.putAdditionalProperty("DataRelationshipReference", dataRelationshipReference);
        }
        return physicalInstance;
    }
    private static Group group(String isUniversallyUnique, String versionDate, String urn, String agency,
                               String id, String version, String versionResponsibility, Citation citation,
                               List<StudyUnitReference> studyUnitReference, List<String> seriesIris, String typeOfGroup) {
        Group group = new Group();
        group.setURN(urn);
        group.setAgency(agency);
        group.setID(id);
        group.setVersion(version);
        group.setVersionResponsibility(versionResponsibility);
        group.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        group.putAdditionalProperty("@versionDate", versionDate);
        if (citation != null) group.putAdditionalProperty("Citation", citation);
        if (studyUnitReference != null) group.putAdditionalProperty("StudyUnitReference", studyUnitReference);
        if (seriesIris != null) group.putAdditionalProperty("seriesIris", seriesIris);
        if (typeOfGroup != null) group.putAdditionalProperty("typeOfGroup", typeOfGroup);
        return group;
    }

    private static StudyUnit studyUnit(String isUniversallyUnique, String versionDate, String urn, String agency,
                                       String id, String version, Citation citation, String operationIri,
                                       List<DDIReference> physicalInstanceReferences) {
        StudyUnit studyUnit = new StudyUnit();
        studyUnit.setURN(urn);
        studyUnit.setAgency(agency);
        studyUnit.setID(id);
        studyUnit.setVersion(version);
        studyUnit.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        studyUnit.putAdditionalProperty("@versionDate", versionDate);
        if (citation != null) studyUnit.putAdditionalProperty("Citation", citation);
        if (operationIri != null) studyUnit.putAdditionalProperty("operationIri", operationIri);
        if (physicalInstanceReferences != null) studyUnit.putAdditionalProperty("physicalInstanceReferences", physicalInstanceReferences);
        return studyUnit;
    }
}
