package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

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
                null);                      // Category

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"$schema\"");
        assertThat(json).contains("\"TopLevelReference\"");
        assertThat(json).doesNotContain("\"PhysicalInstance\"");
        assertThat(json).doesNotContain("\"Variable\"");
        assertThat(json).doesNotContain("\"CodeList\"");
        assertThat(json).doesNotContain("\"Category\"");
        assertThat(json).doesNotContain("null");
    }

    @Test
    void ddi4GroupResponse_omitsNullFields() throws Exception {
        Ddi4GroupResponse response = new Ddi4GroupResponse(
                Ddi4Response.SCHEMA,
                null,                       // TopLevelReference
                null,                       // Group
                null);                      // StudyUnit

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"$schema\"");
        assertThat(json).doesNotContain("null");
    }

    @Test
    void ddi4CodeListResponse_omitsNullFields() throws Exception {
        Ddi4CodeListResponse response = new Ddi4CodeListResponse(
                Ddi4Response.SCHEMA,
                null,                       // TopLevelReference
                null);                      // CodeList

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"$schema\"");
        assertThat(json).doesNotContain("null");
    }
}
