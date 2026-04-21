package fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupDDIItemConverterTest {

    private static final String GROUP_XML = """
            <Fragment xmlns="ddi:instance:3_3">
                <ddi:Group isUniversallyUnique="true" versionDate="2026-04-05T17:25:23.503755+01:00"
                    xmlns:ddi="ddi:group:3_3">
                    <r:URN xmlns:r="ddi:reusable:3_3">urn:ddi:fr.insee:7cebe742-1257-3286-9f02-6977a0989809:1</r:URN>
                    <r:Agency xmlns:r="ddi:reusable:3_3">fr.insee</r:Agency>
                    <r:ID xmlns:r="ddi:reusable:3_3">7cebe742-1257-3286-9f02-6977a0989809</r:ID>
                    <r:Version xmlns:r="ddi:reusable:3_3">1</r:Version>
                    <r:UserID typeOfUserID="URI" xmlns:r="ddi:reusable:3_3">http://bauhaus/operations/serie/s1002</r:UserID>
                    <ddi:TypeOfGroup>insee:StatisticalOperationSeries</ddi:TypeOfGroup>
                    <r:Citation xmlns:r="ddi:reusable:3_3">
                        <r:Title>
                            <r:String xml:lang="fr-FR">Enquête auprès des sans-domicile Group</r:String>
                        </r:Title>
                    </r:Citation>
                    <r:StudyUnitReference xmlns:r="ddi:reusable:3_3">
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>c2acc8ab-f73e-3bd7-a387-1aab8db15efb</r:ID>
                        <r:Version>1</r:Version>
                        <r:TypeOfObject>StudyUnit</r:TypeOfObject>
                    </r:StudyUnitReference>
                    <r:StudyUnitReference xmlns:r="ddi:reusable:3_3">
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>cf6da9a8-066a-3a1d-a759-8a029085c0a9</r:ID>
                        <r:Version>1</r:Version>
                        <r:TypeOfObject>StudyUnit</r:TypeOfObject>
                    </r:StudyUnitReference>
                </ddi:Group>
            </Fragment>
            """;

    private GroupDDIItemConverter converter;

    @BeforeEach
    void setUp() {
        converter = new GroupDDIItemConverter();
    }

    @Test
    void supports_Group() {
        assertTrue(converter.supports("Group"));
    }

    @Test
    void doesNotSupport_otherTypes() {
        assertFalse(converter.supports("StudyUnit"));
        assertFalse(converter.supports("CodeList"));
        assertFalse(converter.supports("PhysicalInstance"));
    }

    @Test
    void convert_mapsVersionableFields() {
        JsonNode result = converter.convert(GROUP_XML);

        assertEquals("urn:ddi:fr.insee:7cebe742-1257-3286-9f02-6977a0989809:1", result.get("URN").asText());
        assertEquals("fr.insee", result.get("Agency").asText());
        assertEquals("7cebe742-1257-3286-9f02-6977a0989809", result.get("ID").asText());
        assertEquals("1", result.get("Version").asText());
    }

    @Test
    void convert_mapsVersionDate() {
        JsonNode result = converter.convert(GROUP_XML);

        JsonNode versionDate = result.get("VersionDate");
        assertNotNull(versionDate);
        assertEquals("2026-04-05T17:25:23.503755+01:00", versionDate.get("DateTime").asText());
    }

    @Test
    void convert_mapsUserId() {
        JsonNode result = converter.convert(GROUP_XML);

        JsonNode userIds = result.get("UserID");
        assertNotNull(userIds);
        assertEquals(1, userIds.size());

        JsonNode userId = userIds.get(0);
        assertEquals("http://bauhaus/operations/serie/s1002", userId.get("StringValue").asText());
        assertEquals("URI", userId.get("TypeOfUserID").get("StringValue").asText());
    }

    @Test
    void convert_mapsTypeOfGroup() {
        JsonNode result = converter.convert(GROUP_XML);

        JsonNode typeOfGroup = result.get("TypeOfGroup");
        assertNotNull(typeOfGroup);
        assertEquals("insee:StatisticalOperationSeries", typeOfGroup.get("StringValue").asText());
    }

    @Test
    void convert_mapsCitationTitle() {
        JsonNode result = converter.convert(GROUP_XML);

        JsonNode citation = result.get("Citation");
        assertNotNull(citation);

        JsonNode titleStrings = citation.get("Title").get("String");
        assertNotNull(titleStrings);
        assertEquals(1, titleStrings.size());

        JsonNode multilingualValue = titleStrings.get(0).get("MultilingualStringValue");
        assertEquals("fr-FR", multilingualValue.get("LanguageTag").asText());
        assertEquals("Enquête auprès des sans-domicile Group", multilingualValue.get("Value").asText());
    }

    @Test
    void convert_mapsStudyUnitReferences() {
        JsonNode result = converter.convert(GROUP_XML);

        JsonNode refs = result.get("StudyUnitReference");
        assertNotNull(refs);
        assertEquals(2, refs.size());

        JsonNode ref1 = refs.get(0);
        assertEquals("StudyUnit", ref1.get("$type").asText());
        assertEquals("urn:ddi:fr.insee:c2acc8ab-f73e-3bd7-a387-1aab8db15efb:1", ref1.get("value").get(0).asText());

        JsonNode ref2 = refs.get(1);
        assertEquals("StudyUnit", ref2.get("$type").asText());
        assertEquals("urn:ddi:fr.insee:cf6da9a8-066a-3a1d-a759-8a029085c0a9:1", ref2.get("value").get(0).asText());
    }

    @Test
    void convert_throwsWhenNoGroupElement() {
        String badXml = "<Fragment xmlns=\"ddi:instance:3_3\"><SomethingElse/></Fragment>";
        assertThrows(RuntimeException.class, () -> converter.convert(badXml));
    }
}
