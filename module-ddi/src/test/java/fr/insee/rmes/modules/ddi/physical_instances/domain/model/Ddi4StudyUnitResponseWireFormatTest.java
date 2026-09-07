package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Contrat de fil de la sortie de {@code GET /ddi/operation/{id}/fichiers} en JSON : la même
 * enveloppe {@code topLevelReferences} + {@code items} que {@link Ddi4Response} et
 * {@link Ddi4GroupResponse}, la StudyUnit y voisinant avec les PhysicalInstances déréférencées
 * (#1145), chacune discriminée par son {@code $type}.
 */
class Ddi4StudyUnitResponseWireFormatTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static Ddi4StudyUnitResponse aResponse() {
        return new Ddi4StudyUnitResponse(
                Ddi4Response.SCHEMA,
                List.of(Reference.of("fr.insee", "su-1", "1", Ddi4StudyUnit.TYPE)),
                List.of(new Ddi4StudyUnit(
                        Ddi4StudyUnit.TYPE, null, "urn:ddi:fr.insee:su-1:1", "fr.insee", "su-1", "1",
                        new Citation(LangStrings.of("fr-FR", "Mon opération")),
                        "http://id.insee.fr/operations/operation/op1",
                        List.of(Reference.of("fr.insee", "pi-1", "1", Ddi4PhysicalInstance.TYPE)))),
                List.of(new Ddi4PhysicalInstance(
                        Ddi4PhysicalInstance.TYPE, null, "urn:ddi:fr.insee:pi-1:1", "fr.insee",
                        "pi-1", "1", null, new Citation(LangStrings.of("fr-FR", "Ma PI")), null)));
    }

    @Test
    void shouldSerializeToTheSchemaEnvelope() throws Exception {
        JsonNode json = mapper.readTree(mapper.writeValueAsString(aResponse()));

        assertThat(json.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("topLevelReferences", "items");
    }

    @Test
    void shouldListTheStudyUnitThenItsPhysicalInstances() throws Exception {
        JsonNode items = mapper.readTree(mapper.writeValueAsString(aResponse())).get("items");

        assertThat(items).hasSize(2);
        assertThat(items).extracting(item -> item.get("$type").asText())
                .containsExactly("StudyUnit", "PhysicalInstance");
        assertThat(items).extracting(item -> item.get("ID").asText())
                .containsExactly("su-1", "pi-1");
    }

    @Test
    void shouldDeserializeItemsBackIntoTypedLists() throws Exception {
        String wire = mapper.writeValueAsString(aResponse());

        Ddi4StudyUnitResponse roundTripped = mapper.readValue(wire, Ddi4StudyUnitResponse.class);

        assertThat(roundTripped.studyUnit()).extracting(Ddi4StudyUnit::id).containsExactly("su-1");
        assertThat(roundTripped.physicalInstance()).extracting(Ddi4PhysicalInstance::id)
                .containsExactly("pi-1");
        assertThat(roundTripped.topLevelReference()).extracting(Reference::id).containsExactly("su-1");
    }

    @Test
    void shouldOmitEmptyEnvelopeMembers() throws Exception {
        Ddi4StudyUnitResponse empty =
                new Ddi4StudyUnitResponse(Ddi4Response.SCHEMA, null, null, null);

        String json = mapper.writeValueAsString(empty);

        assertThat(json).doesNotContain("null").doesNotContain("topLevelReferences");
    }
}
