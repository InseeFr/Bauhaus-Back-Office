package fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalInstanceDDIItemConverterTest {

    private static final String PHYSICAL_INSTANCE_XML = """
            <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                <PhysicalInstance isUniversallyUnique="true" versionDate="2026-04-05T17:25:21.011124+01:00"
                    xmlns="ddi:physicalinstance:3_3">
                    <r:URN>urn:ddi:fr.insee:c05c0443-fc56-4069-9bea-a9c7300ae0a0:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>c05c0443-fc56-4069-9bea-a9c7300ae0a0</r:ID>
                    <r:Version>1</r:Version>
                    <r:Citation>
                        <r:Title>
                            <r:String xml:lang="fr-FR">Enquête auprès des personnes fréquentant les services d'hébergement ou de distribution de repas - 2012 Physical Instance</r:String>
                        </r:Title>
                    </r:Citation>
                    <r:DataRelationshipReference>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>e1099551-32c7-4ccb-9fa9-071553f319ac</r:ID>
                        <r:Version>1</r:Version>
                        <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                    </r:DataRelationshipReference>
                </PhysicalInstance>
            </Fragment>
            """;

    private PhysicalInstanceDDIItemConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PhysicalInstanceDDIItemConverter();
    }

    @Test
    void supports_PhysicalInstance() {
        assertTrue(converter.supports("PhysicalInstance"));
    }

    @Test
    void doesNotSupport_otherTypes() {
        assertFalse(converter.supports("Group"));
        assertFalse(converter.supports("StudyUnit"));
    }

    @Test
    void convert_mapsVersionableFields() {
        JsonNode result = converter.convert(PHYSICAL_INSTANCE_XML);

        assertEquals("urn:ddi:fr.insee:c05c0443-fc56-4069-9bea-a9c7300ae0a0:1", result.get("URN").asText());
        assertEquals("fr.insee", result.get("Agency").asText());
        assertEquals("c05c0443-fc56-4069-9bea-a9c7300ae0a0", result.get("ID").asText());
        assertEquals("1", result.get("Version").asText());
    }

    @Test
    void convert_mapsVersionDate() {
        JsonNode result = converter.convert(PHYSICAL_INSTANCE_XML);

        assertEquals("2026-04-05T17:25:21.011124+01:00", result.get("VersionDate").get("DateTime").asText());
    }

    @Test
    void convert_noUserIdForPhysicalInstance() {
        JsonNode result = converter.convert(PHYSICAL_INSTANCE_XML);

        assertNull(result.get("UserID"));
    }

    @Test
    void convert_mapsCitationTitle() {
        JsonNode result = converter.convert(PHYSICAL_INSTANCE_XML);

        JsonNode titleStrings = result.get("Citation").get("Title").get("String");
        assertEquals(1, titleStrings.size());
        JsonNode multilingualValue = titleStrings.get(0).get("MultilingualStringValue");
        assertEquals("fr-FR", multilingualValue.get("LanguageTag").asText());
        assertTrue(multilingualValue.get("Value").asText().contains("2012 Physical Instance"));
    }

    @Test
    void convert_mapsDataRelationshipReference() {
        JsonNode result = converter.convert(PHYSICAL_INSTANCE_XML);

        JsonNode refs = result.get("DataRelationshipReference");
        assertNotNull(refs);
        assertEquals(1, refs.size());
        assertEquals("DataRelationship", refs.get(0).get("$type").asText());
        assertEquals("urn:ddi:fr.insee:e1099551-32c7-4ccb-9fa9-071553f319ac:1", refs.get(0).get("value").get(0).asText());
    }
}
