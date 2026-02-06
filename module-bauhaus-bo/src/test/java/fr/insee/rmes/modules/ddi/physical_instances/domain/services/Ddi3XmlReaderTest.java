package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Ddi3XmlReaderTest {

    private Ddi3XmlReader reader;

    @BeforeEach
    void setUp() {
        reader = new Ddi3XmlReader();
    }

    @Test
    void shouldParsePhysicalInstanceWithBasedOnObject() throws Exception {
        // Given - XML with BasedOnObject
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:f3ba3ad8-7b0a-4ede-9950-5e6169ebaeae:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>f3ba3ad8-7b0a-4ede-9950-5e6169ebaeae</r:ID>
                    <r:Version>1</r:Version>
                    <r:BasedOnObject>
                        <r:BasedOnReference>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>original-pi-id</r:ID>
                            <r:Version>1</r:Version>
                            <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
                        </r:BasedOnReference>
                    </r:BasedOnObject>
                    <r:Citation>
                        <r:Title>
                            <r:String xml:lang="fr-FR">Test Instance</r:String>
                        </r:Title>
                    </r:Citation>
                </PhysicalInstance>
            </Fragment>
            """;

        // When
        Ddi4PhysicalInstance result = reader.parsePhysicalInstance(xml);

        // Then
        assertNotNull(result);
        assertEquals("fr.insee", result.agency());
        assertEquals("f3ba3ad8-7b0a-4ede-9950-5e6169ebaeae", result.id());

        // Verify BasedOnObject
        assertNotNull(result.basedOnObject());
        assertNotNull(result.basedOnObject().basedOnReference());
        assertEquals("fr.insee", result.basedOnObject().basedOnReference().agency());
        assertEquals("original-pi-id", result.basedOnObject().basedOnReference().id());
        assertEquals("1", result.basedOnObject().basedOnReference().version());
        assertEquals("PhysicalInstance", result.basedOnObject().basedOnReference().typeOfObject());
    }

    @Test
    void shouldParsePhysicalInstanceWithoutBasedOnObject() throws Exception {
        // Given - XML without BasedOnObject
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:f3ba3ad8-7b0a-4ede-9950-5e6169ebaeae:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>f3ba3ad8-7b0a-4ede-9950-5e6169ebaeae</r:ID>
                    <r:Version>1</r:Version>
                    <r:Citation>
                        <r:Title>
                            <r:String xml:lang="fr-FR">Test Instance</r:String>
                        </r:Title>
                    </r:Citation>
                </PhysicalInstance>
            </Fragment>
            """;

        // When
        Ddi4PhysicalInstance result = reader.parsePhysicalInstance(xml);

        // Then
        assertNotNull(result);
        assertNull(result.basedOnObject());
    }

    @Test
    void shouldParseDataRelationshipWithBasedOnObject() throws Exception {
        // Given - XML with BasedOnObject
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
                    <r:BasedOnObject>
                        <r:BasedOnReference>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>original-dr-id</r:ID>
                            <r:Version>1</r:Version>
                            <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                        </r:BasedOnReference>
                    </r:BasedOnObject>
                    <DataRelationshipName>
                        <r:String xml:lang="fr-FR">Test DR</r:String>
                    </DataRelationshipName>
                </DataRelationship>
            </Fragment>
            """;

        // When
        Ddi4DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertEquals("fr.insee", result.agency());
        assertEquals("dr-id", result.id());

        // Verify BasedOnObject
        assertNotNull(result.basedOnObject());
        assertNotNull(result.basedOnObject().basedOnReference());
        assertEquals("fr.insee", result.basedOnObject().basedOnReference().agency());
        assertEquals("original-dr-id", result.basedOnObject().basedOnReference().id());
        assertEquals("DataRelationship", result.basedOnObject().basedOnReference().typeOfObject());
    }

    @Test
    void shouldParseVariableWithBasedOnObject() throws Exception {
        // Given - XML with BasedOnObject
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>var-id</r:ID>
                    <r:Version>1</r:Version>
                    <r:BasedOnObject>
                        <r:BasedOnReference>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>original-var-id</r:ID>
                            <r:Version>1</r:Version>
                            <r:TypeOfObject>Variable</r:TypeOfObject>
                        </r:BasedOnReference>
                    </r:BasedOnObject>
                    <VariableName>
                        <r:String xml:lang="fr-FR">TEST_VAR</r:String>
                    </VariableName>
                    <r:Label>
                        <r:Content xml:lang="fr-FR">Test Variable</r:Content>
                    </r:Label>
                </Variable>
            </Fragment>
            """;

        // When
        Ddi4Variable result = reader.parseVariable(xml);

        // Then
        assertNotNull(result);
        assertEquals("fr.insee", result.agency());
        assertEquals("var-id", result.id());

        // Verify BasedOnObject
        assertNotNull(result.basedOnObject());
        assertNotNull(result.basedOnObject().basedOnReference());
        assertEquals("fr.insee", result.basedOnObject().basedOnReference().agency());
        assertEquals("original-var-id", result.basedOnObject().basedOnReference().id());
        assertEquals("Variable", result.basedOnObject().basedOnReference().typeOfObject());
    }

    @Test
    void shouldParseDataRelationshipWithLabel() throws Exception {
        // Given - XML with Label
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
                    <DataRelationshipName>
                        <r:String xml:lang="fr-FR">Test DR Name</r:String>
                    </DataRelationshipName>
                    <r:Label>
                        <r:Content xml:lang="fr-FR">Test DR Label</r:Content>
                    </r:Label>
                </DataRelationship>
            </Fragment>
            """;

        // When
        Ddi4DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertEquals("dr-id", result.id());
        assertNotNull(result.label());
        assertNotNull(result.label().content());
        assertEquals("fr-FR", result.label().content().xmlLang());
        assertEquals("Test DR Label", result.label().content().text());
    }

    @Test
    void shouldParseDataRelationshipWithoutLabel() throws Exception {
        // Given - XML without Label
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
                    <DataRelationshipName>
                        <r:String xml:lang="fr-FR">Test DR Name</r:String>
                    </DataRelationshipName>
                </DataRelationship>
            </Fragment>
            """;

        // When
        Ddi4DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertEquals("dr-id", result.id());
        assertNull(result.label());
    }

    @Test
    void shouldParseLogicalRecordWithLabel() throws Exception {
        // Given - XML with LogicalRecord containing Label
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
                    <DataRelationshipName>
                        <r:String xml:lang="fr-FR">Test DR</r:String>
                    </DataRelationshipName>
                    <LogicalRecord isUniversallyUnique="true">
                        <r:URN>urn:ddi:fr.insee:lr-id:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>lr-id</r:ID>
                        <r:Version>1</r:Version>
                        <LogicalRecordName>
                            <r:String xml:lang="fr-FR">LR Name</r:String>
                        </LogicalRecordName>
                        <r:Label>
                            <r:Content xml:lang="fr-FR">LR Label</r:Content>
                        </r:Label>
                    </LogicalRecord>
                </DataRelationship>
            </Fragment>
            """;

        // When
        Ddi4DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertNotNull(result.logicalRecord());
        assertEquals("lr-id", result.logicalRecord().id());
        assertNotNull(result.logicalRecord().label());
        assertNotNull(result.logicalRecord().label().content());
        assertEquals("fr-FR", result.logicalRecord().label().content().xmlLang());
        assertEquals("LR Label", result.logicalRecord().label().content().text());
    }

    @Test
    void shouldParseLogicalRecordWithoutLabel() throws Exception {
        // Given - XML with LogicalRecord without Label
        String xml = """
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
                    <DataRelationshipName>
                        <r:String xml:lang="fr-FR">Test DR</r:String>
                    </DataRelationshipName>
                    <LogicalRecord isUniversallyUnique="true">
                        <r:URN>urn:ddi:fr.insee:lr-id:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>lr-id</r:ID>
                        <r:Version>1</r:Version>
                        <LogicalRecordName>
                            <r:String xml:lang="fr-FR">LR Name</r:String>
                        </LogicalRecordName>
                    </LogicalRecord>
                </DataRelationship>
            </Fragment>
            """;

        // When
        Ddi4DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertNotNull(result.logicalRecord());
        assertEquals("lr-id", result.logicalRecord().id());
        assertNull(result.logicalRecord().label());
    }
}
