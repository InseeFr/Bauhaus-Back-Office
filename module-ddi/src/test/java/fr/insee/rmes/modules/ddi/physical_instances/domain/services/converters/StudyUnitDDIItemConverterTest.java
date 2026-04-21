package fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudyUnitDDIItemConverterTest {

    private static final String STUDY_UNIT_XML = """
            <Fragment xmlns="ddi:instance:3_3">
                <ddi:StudyUnit isUniversallyUnique="true" versionDate="2026-04-05T17:25:20.909165+01:00"
                    xmlns:ddi="ddi:studyunit:3_3">
                    <r:URN xmlns:r="ddi:reusable:3_3">urn:ddi:fr.insee:c2acc8ab-f73e-3bd7-a387-1aab8db15efb:1</r:URN>
                    <r:Agency xmlns:r="ddi:reusable:3_3">fr.insee</r:Agency>
                    <r:ID xmlns:r="ddi:reusable:3_3">c2acc8ab-f73e-3bd7-a387-1aab8db15efb</r:ID>
                    <r:Version xmlns:r="ddi:reusable:3_3">1</r:Version>
                    <r:UserID typeOfUserID="URI" xmlns:r="ddi:reusable:3_3">http://bauhaus/operations/operation/s1268</r:UserID>
                    <r:Citation xmlns:r="ddi:reusable:3_3">
                        <r:Title>
                            <r:String xml:lang="fr-FR">Enquête auprès des personnes fréquentant les services d'hébergement ou de distribution de repas - 2012 Study Unit</r:String>
                        </r:Title>
                    </r:Citation>
                    <r:PhysicalInstanceReference xmlns:r="ddi:reusable:3_3">
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>c05c0443-fc56-4069-9bea-a9c7300ae0a0</r:ID>
                        <r:Version>1</r:Version>
                        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
                    </r:PhysicalInstanceReference>
                </ddi:StudyUnit>
            </Fragment>
            """;

    private StudyUnitDDIItemConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StudyUnitDDIItemConverter();
    }

    @Test
    void supports_StudyUnit() {
        assertTrue(converter.supports("StudyUnit"));
    }

    @Test
    void doesNotSupport_otherTypes() {
        assertFalse(converter.supports("Group"));
        assertFalse(converter.supports("PhysicalInstance"));
    }

    @Test
    void convert_mapsVersionableFields() {
        JsonNode result = converter.convert(STUDY_UNIT_XML);

        assertEquals("urn:ddi:fr.insee:c2acc8ab-f73e-3bd7-a387-1aab8db15efb:1", result.get("URN").asText());
        assertEquals("fr.insee", result.get("Agency").asText());
        assertEquals("c2acc8ab-f73e-3bd7-a387-1aab8db15efb", result.get("ID").asText());
        assertEquals("1", result.get("Version").asText());
    }

    @Test
    void convert_mapsVersionDate() {
        JsonNode result = converter.convert(STUDY_UNIT_XML);

        assertEquals("2026-04-05T17:25:20.909165+01:00", result.get("VersionDate").get("DateTime").asText());
    }

    @Test
    void convert_mapsUserId() {
        JsonNode result = converter.convert(STUDY_UNIT_XML);

        JsonNode userIds = result.get("UserID");
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertEquals("http://bauhaus/operations/operation/s1268", userIds.get(0).get("StringValue").asText());
        assertEquals("URI", userIds.get(0).get("TypeOfUserID").get("StringValue").asText());
    }

    @Test
    void convert_mapsCitationTitle() {
        JsonNode result = converter.convert(STUDY_UNIT_XML);

        JsonNode titleStrings = result.get("Citation").get("Title").get("String");
        assertEquals(1, titleStrings.size());
        JsonNode multilingualValue = titleStrings.get(0).get("MultilingualStringValue");
        assertEquals("fr-FR", multilingualValue.get("LanguageTag").asText());
        assertTrue(multilingualValue.get("Value").asText().contains("2012 Study Unit"));
    }

    @Test
    void convert_mapsPhysicalInstanceReference() {
        JsonNode result = converter.convert(STUDY_UNIT_XML);

        JsonNode refs = result.get("PhysicalInstanceReference");
        assertNotNull(refs);
        assertEquals(1, refs.size());
        assertEquals("PhysicalInstance", refs.get(0).get("$type").asText());
        assertEquals("urn:ddi:fr.insee:c05c0443-fc56-4069-9bea-a9c7300ae0a0:1", refs.get(0).get("value").get(0).asText());
    }
}
