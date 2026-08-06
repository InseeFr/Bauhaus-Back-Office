package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #494 : la sortie JSON des endpoints DDI ne doit pas porter de champs {@code null}
 * inutiles (« {@code "PhysicalInstance": null} », « {@code "Variable": null} », …). Seuls les
 * champs effectivement renseignés sont sérialisés.
 */
class Ddi4ResponseSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void ddi4Response_omitsNullFields() throws Exception {
        Ddi4Response response = new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(Reference.of("fr.insee", "dr-1", "1", "DataRelationship")),
                null,                       // PhysicalInstance
                List.of(),                  // DataRelationship (présent mais vide)
                null,                       // Variable
                null,                       // CodeList
                null,                       // Category
                null);                      // ManagedMissingValuesRepresentation

        String json = mapper.writeValueAsString(response);

        assertThat(json)
                .contains("\"$schema\"")
                .contains("\"TopLevelReference\"")
                .doesNotContain("\"PhysicalInstance\"")
                .doesNotContain("\"Variable\"")
                .doesNotContain("\"CodeList\"")
                .doesNotContain("\"Category\"")
                .doesNotContain("null");
    }

    @Test
    void ddi4GroupResponse_omitsNullFields() throws Exception {
        Ddi4GroupResponse response = new Ddi4GroupResponse(
                Ddi4Response.SCHEMA,
                null,                       // TopLevelReference
                null,                       // Group
                null);                      // StudyUnit

        String json = mapper.writeValueAsString(response);

        assertThat(json)
                .contains("\"$schema\"")
                .doesNotContain("null");
    }

    @Test
    void ddi4CodeList_omitsNullFields() throws Exception {
        Ddi4CodeList flatCodeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                null,                       // VersionDate
                "urn:ddi:fr.insee:cl-1:1",
                "fr.insee", "cl-1", "1",
                LangStrings.of("fr-FR", "liste plate"),
                null,                       // Level
                null);                      // Code

        String json = mapper.writeValueAsString(flatCodeList);

        assertThat(json)
                .contains("\"$type\"")
                .contains("\"Label\"")
                .doesNotContain("\"Level\"")
                .doesNotContain("\"Code\"")
                .doesNotContain("null");
    }

    /**
     * Valeurs sentinelles (#1566) : la réponse DDI 4 transporte les
     * {@code ManagedMissingValuesRepresentation} référencées par les variables.
     */
    @Test
    void ddi4Response_carriesManagedMissingValuesRepresentations() throws Exception {
        String json = """
                {
                    "$schema": "ddi:4.0",
                    "ManagedMissingValuesRepresentation": [
                        {
                            "$type": "ManagedMissingValuesRepresentation",
                            "URN": "urn:ddi:fr.insee:mmvr-1:1",
                            "Agency": "fr.insee",
                            "ID": "mmvr-1",
                            "Version": "1",
                            "Label": [{"@language": "fr-FR", "@value": "Valeurs sentinelles NSP/REF"}]
                        }
                    ]
                }
                """;
        ObjectMapper tolerantMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Ddi4Response response = tolerantMapper.readValue(json, Ddi4Response.class);
        String out = tolerantMapper.writeValueAsString(response);

        assertThat(out)
                .contains("\"ManagedMissingValuesRepresentation\"")
                .contains("\"urn:ddi:fr.insee:mmvr-1:1\"");
    }

    @Test
    void ddi4CodeListResponse_omitsNullFields() throws Exception {
        Ddi4CodeListResponse response = new Ddi4CodeListResponse(
                Ddi4Response.SCHEMA,
                null,                       // TopLevelReference
                null);                      // CodeList

        String json = mapper.writeValueAsString(response);

        assertThat(json)
                .contains("\"$schema\"")
                .doesNotContain("null");
    }

    /**
     * Contrat de fil pour les variantes (liste ou catégorie créée depuis une popup de partage) :
     * le JSON produit par le front doit se désérialiser en {@code BasedOnObject}. Sans ce test,
     * un écart de nom de propriété passerait inaperçu — Jackson ignore silencieusement les
     * propriétés inconnues.
     */
    @Test
    void shouldDeserializeBasedOnObjectSentByTheFrontOnCategoryAndCodeList() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String categoryJson = """
            {
              "$type": "Category",
              "VersionDate": { "DateTime": "2026-08-06T10:00:00.000Z" },
              "URN": "urn:ddi:fr.insee:variant-cat:1",
              "Agency": "fr.insee",
              "ID": "variant-cat",
              "Version": "1",
              "BasedOnObject": {
                "$type": "BasedOnObjectType",
                "BasedOnReference": [
                  {
                    "$type": "Category",
                    "URN": "urn:ddi:fr.insee:original-cat:3",
                    "Agency": "fr.insee",
                    "ID": "original-cat",
                    "Version": "3"
                  }
                ]
              },
              "Label": [ { "@language": "fr-FR", "@value": "Europe variante" } ]
            }
            """;

        Ddi4Category category = mapper.readValue(categoryJson, Ddi4Category.class);

        assertThat(category.basedOnObject()).isNotNull();
        assertThat(category.basedOnObject().basedOnReferences()).hasSize(1);
        assertThat(category.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-cat");
        assertThat(category.basedOnObject().basedOnReferences().get(0).type()).isEqualTo("Category");

        String codeListJson = """
            {
              "$type": "CodeList",
              "URN": "urn:ddi:fr.insee:variant-cl:1",
              "Agency": "fr.insee",
              "ID": "variant-cl",
              "Version": "1",
              "BasedOnObject": {
                "$type": "BasedOnObjectType",
                "BasedOnReference": [
                  {
                    "$type": "CodeList",
                    "URN": "urn:ddi:fr.insee:original-cl:2",
                    "Agency": "fr.insee",
                    "ID": "original-cl",
                    "Version": "2"
                  }
                ]
              }
            }
            """;

        Ddi4CodeList codeList = mapper.readValue(codeListJson, Ddi4CodeList.class);

        assertThat(codeList.basedOnObject()).isNotNull();
        assertThat(codeList.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-cl");
    }
}
