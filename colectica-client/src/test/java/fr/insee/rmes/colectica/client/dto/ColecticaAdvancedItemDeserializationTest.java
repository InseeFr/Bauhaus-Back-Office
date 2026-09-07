package fr.insee.rmes.colectica.client.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ColecticaAdvancedItemDeserializationTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void shouldDeserializeRealAdvancedQueryItemWithPropertyBags() {
        String json = """
            {
              "AgencyId": "fr.insee",
              "Identifier": "2ded665b-f513-489a-8c7a-8778f5ffc7de",
              "Version": 1,
              "CompositeId": {"Item1": "2ded665b-f513-489a-8c7a-8778f5ffc7de", "Item2": 1, "Item3": "fr.insee"},
              "IsDeprecated": false,
              "ItemType": "a51e85bb-6259-4488-8df2-f08cb43485f8",
              "TextProperties": {
                "versionResponsibility": [{"Value": "demey.emmanuel@gmail.com", "LanguageTag": "en-US"}],
                "dcTitle": [{"Value": "20260625 EDE", "LanguageTag": "fr-FR"}],
                "label": [{"Value": "20260625 EDE", "LanguageTag": "fr-FR"}]
              },
              "DateProperties": {"versionDate": ["2026-06-29T14:26:32.961778"]},
              "BooleanProperties": {"isPublished": false},
              "DecimalProperties": {},
              "CodeValueProperties": {},
              "LocatorProperties": {},
              "Tags": [],
              "InternalRowId": 96091
            }
            """;

        ColecticaAdvancedItem item =
            assertDoesNotThrow(() -> mapper.readValue(json, ColecticaAdvancedItem.class));

        assertEquals("fr.insee", item.agencyId());
        assertEquals("2ded665b-f513-489a-8c7a-8778f5ffc7de", item.identifier());
        assertEquals(1, item.version());
        assertEquals("a51e85bb-6259-4488-8df2-f08cb43485f8", item.itemType());
        assertFalse(item.isDeprecated());
        assertEquals("20260625 EDE", item.textProperties().get("label").get(0).value());
        assertEquals("fr-FR", item.textProperties().get("label").get(0).languageTag());
        assertEquals("20260625 EDE", item.textProperties().get("dcTitle").get(0).value());
        assertEquals("2026-06-29T14:26:32.961778", item.dateProperties().get("versionDate").get(0));
        assertEquals(Boolean.FALSE, item.booleanProperties().get("isPublished"));
    }

    @Test
    void shouldDeserializeWhenPropertyBagsAreAbsent() {
        String json = """
            {
              "AgencyId": "fr.insee",
              "Identifier": "abcd-1234",
              "ItemType": "a51e85bb-6259-4488-8df2-f08cb43485f8"
            }
            """;

        ColecticaAdvancedItem item =
            assertDoesNotThrow(() -> mapper.readValue(json, ColecticaAdvancedItem.class));

        assertEquals("abcd-1234", item.identifier());
        assertNull(item.textProperties());
        assertNull(item.dateProperties());
    }
}
