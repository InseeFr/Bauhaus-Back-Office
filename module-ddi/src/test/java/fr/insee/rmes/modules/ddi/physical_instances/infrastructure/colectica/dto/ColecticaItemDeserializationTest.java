package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ColecticaItemDeserializationTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void shouldDeserializeWhenMetadataRankIsNull() {
        String json = """
            {
              "AgencyId": "fr.insee",
              "Identifier": "abcd-1234",
              "MetadataRank": null
            }
            """;

        ColecticaItem item = assertDoesNotThrow(() -> mapper.readValue(json, ColecticaItem.class));

        assertEquals("fr.insee", item.agencyId());
        assertEquals("abcd-1234", item.identifier());
        assertNull(item.metadataRank());
    }

    @Test
    void shouldDeserializeWhenColecticaReturnsNullPrimitiveFields() {
        String json = """
            {
              "Summary": null,
              "ItemName": null,
              "Label": null,
              "Description": null,
              "VersionRationale": null,
              "MetadataRank": null,
              "RepositoryName": null,
              "IsAuthoritative": null,
              "Tags": null,
              "ItemType": "30ea0200-7121-4f01-8d21-a931a182b86d",
              "AgencyId": "fr.insee",
              "Version": null,
              "Identifier": "abcd-1234",
              "Item": null,
              "Notes": null,
              "VersionDate": null,
              "VersionResponsibility": null,
              "IsPublished": null,
              "IsDeprecated": null,
              "IsProvisional": null,
              "ItemFormat": null,
              "TransactionId": null,
              "VersionCreationType": null
            }
            """;

        ColecticaItem item = assertDoesNotThrow(() -> mapper.readValue(json, ColecticaItem.class));

        assertEquals("fr.insee", item.agencyId());
        assertEquals("abcd-1234", item.identifier());
    }
}
