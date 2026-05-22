package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.generated.DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.generated.PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Variable;
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
        PhysicalInstance result = reader.parsePhysicalInstance(xml);

        // Then
        assertNotNull(result);
        assertEquals("fr.insee", result.getAgency());
        assertEquals("f3ba3ad8-7b0a-4ede-9950-5e6169ebaeae", result.getID());

        // Verify BasedOnObject (forme historique portee par additionalProperties)
        BasedOnObject basedOnObject = (BasedOnObject) result.getAdditionalProperties().get("BasedOnObject");
        assertNotNull(basedOnObject);
        assertNotNull(basedOnObject.basedOnReference());
        assertEquals("fr.insee", basedOnObject.basedOnReference().agency());
        assertEquals("original-pi-id", basedOnObject.basedOnReference().id());
        assertEquals("1", basedOnObject.basedOnReference().version());
        assertEquals("PhysicalInstance", basedOnObject.basedOnReference().typeOfObject());
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
        PhysicalInstance result = reader.parsePhysicalInstance(xml);

        // Then
        assertNotNull(result);
        assertNull(result.getAdditionalProperties().get("BasedOnObject"));
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
        DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertEquals("fr.insee", result.getAgency());
        assertEquals("dr-id", result.getID());

        // Verify BasedOnObject (forme historique portee par additionalProperties)
        BasedOnObject basedOnObject = (BasedOnObject) result.getAdditionalProperties().get("BasedOnObject");
        assertNotNull(basedOnObject);
        assertNotNull(basedOnObject.basedOnReference());
        assertEquals("fr.insee", basedOnObject.basedOnReference().agency());
        assertEquals("original-dr-id", basedOnObject.basedOnReference().id());
        assertEquals("DataRelationship", basedOnObject.basedOnReference().typeOfObject());
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
        Variable result = reader.parseVariable(xml);

        // Then
        assertNotNull(result);
        assertEquals("fr.insee", result.getAgency());
        assertEquals("var-id", result.getID());

        // Verify BasedOnObject (forme historique portee par additionalProperties)
        BasedOnObject basedOnObject = (BasedOnObject) result.getAdditionalProperties().get("BasedOnObject");
        assertNotNull(basedOnObject);
        assertNotNull(basedOnObject.basedOnReference());
        assertEquals("fr.insee", basedOnObject.basedOnReference().agency());
        assertEquals("original-var-id", basedOnObject.basedOnReference().id());
        assertEquals("Variable", basedOnObject.basedOnReference().typeOfObject());
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
        DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertEquals("dr-id", result.getID());
        assertNotNull(result.getLabel());
        assertNotNull(result.getLabel().get(0));
        assertEquals("fr-FR", result.getLabel().get(0).getAtLanguage());
        assertEquals("Test DR Label", result.getLabel().get(0).getAtValue());
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
        DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertEquals("dr-id", result.getID());
        assertNotNull(result.getLabel());
        assertEquals("Test DR Name", result.getLabel().get(0).getAtValue());
        assertEquals("fr-FR", result.getLabel().get(0).getAtLanguage());
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
        DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertNotNull(((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")));
        assertEquals("lr-id", ((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).id());
        assertNotNull(((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).label());
        assertNotNull(((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).label().get(0));
        assertEquals("fr-FR", ((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).label().get(0).getAtLanguage());
        assertEquals("LR Label", ((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).label().get(0).getAtValue());
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
        DataRelationship result = reader.parseDataRelationship(xml);

        // Then
        assertNotNull(result);
        assertNotNull(((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")));
        assertEquals("lr-id", ((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).id());
        assertNotNull(((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).label());
        assertEquals("LR Name", ((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).label().get(0).getAtValue());
        assertEquals("fr-FR", ((LogicalRecord) result.getAdditionalProperties().get("LogicalRecord")).label().get(0).getAtLanguage());
    }
}
