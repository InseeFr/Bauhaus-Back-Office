package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ColecticaParentRefDeserializationTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

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

        ColecticaParentRef ref = assertDoesNotThrow(() -> mapper.readValue(json, ColecticaParentRef.class));

        assertEquals("fr.insee", ref.agencyId());
        assertEquals("abcd-1234", ref.identifier());
    }
}
