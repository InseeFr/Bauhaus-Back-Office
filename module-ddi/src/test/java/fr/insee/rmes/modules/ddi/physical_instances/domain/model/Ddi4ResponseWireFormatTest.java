package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Contrat de fil du DDI 4 : ce qui transite avec le front est l'enveloppe du
 * {@code ddi-schema.json}, à savoir {@code topLevelReferences} + {@code items} (tableau à plat,
 * objets discriminés par leur {@code $type}).
 * <p>
 * Le schéma déclare exactement ces deux propriétés à la racine, avec
 * {@code additionalProperties: false} : ni {@code $schema}, ni les clés groupées par type de la
 * sérialisation Colectica ({@code PhysicalInstance}, {@code Variable}, …) n'y ont leur place.
 * Le record garde ses listes typées en interne — seule la projection JSON change.
 */
class Ddi4ResponseWireFormatTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static Ddi4Response aResponse() {
        return new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(Reference.of("fr.insee", "pi-1", "1", Ddi4PhysicalInstance.TYPE)),
                List.of(new Ddi4PhysicalInstance(
                        Ddi4PhysicalInstance.TYPE, null,
                        "urn:ddi:fr.insee:pi-1:1", "fr.insee", "pi-1", "1",
                        null, new Citation(LangStrings.of("fr-FR", "Ma PI")), null)),
                List.of(new Ddi4DataRelationship(
                        Ddi4DataRelationship.TYPE, null,
                        "urn:ddi:fr.insee:dr-1:1", "fr.insee", "dr-1", "1",
                        null, LangStrings.of("fr-FR", "Structure"), null)),
                List.of(new Ddi4Variable(
                        Ddi4Variable.TYPE, null,
                        "urn:ddi:fr.insee:var-1:1", "fr.insee", "var-1", "1",
                        null, LangStrings.of("fr-FR", "AGE"), LangStrings.of("fr-FR", "Âge"),
                        null, null, null)),
                List.of(new Ddi4CodeList(
                        Ddi4CodeList.TYPE, null,
                        "urn:ddi:fr.insee:cl-1:1", "fr.insee", "cl-1", "1",
                        LangStrings.of("fr-FR", "Liste"), null, null)),
                List.of(new Ddi4Category(
                        Ddi4Category.TYPE, null,
                        "urn:ddi:fr.insee:cat-1:1", "fr.insee", "cat-1", "1",
                        LangStrings.of("fr-FR", "Oui"))),
                List.of(new Ddi4ManagedMissingValuesRepresentation(
                        Ddi4ManagedMissingValuesRepresentation.TYPE, null,
                        "urn:ddi:fr.insee:mmvr-1:1", "fr.insee", "mmvr-1", "1",
                        LangStrings.of("fr-FR", "NSP/REF"), null)));
    }

    @Test
    void shouldSerializeToTheSchemaEnvelope() throws Exception {
        JsonNode json = mapper.readTree(mapper.writeValueAsString(aResponse()));

        assertThat(json.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("topLevelReferences", "items");
    }

    @Test
    void shouldFlattenEveryTypedListIntoItems() throws Exception {
        JsonNode items = mapper.readTree(mapper.writeValueAsString(aResponse())).get("items");

        assertThat(items).hasSize(6);
        assertThat(items).allSatisfy(item -> assertThat(item.has("$type")).isTrue());
        assertThat(items).extracting(item -> item.get("$type").asText())
                .containsExactlyInAnyOrder("PhysicalInstance", "DataRelationship", "Variable",
                        "CodeList", "Category", "ManagedMissingValuesRepresentation");
    }

    @Test
    void shouldDeserializeItemsBackIntoTypedLists() throws Exception {
        String wire = mapper.writeValueAsString(aResponse());

        Ddi4Response roundTripped = mapper.readValue(wire, Ddi4Response.class);

        assertThat(roundTripped.physicalInstance()).extracting(Ddi4PhysicalInstance::id)
                .containsExactly("pi-1");
        assertThat(roundTripped.dataRelationship()).extracting(Ddi4DataRelationship::id)
                .containsExactly("dr-1");
        assertThat(roundTripped.variable()).extracting(Ddi4Variable::id).containsExactly("var-1");
        assertThat(roundTripped.codeList()).extracting(Ddi4CodeList::id).containsExactly("cl-1");
        assertThat(roundTripped.category()).extracting(Ddi4Category::id).containsExactly("cat-1");
        assertThat(roundTripped.managedMissingValuesRepresentation())
                .extracting(Ddi4ManagedMissingValuesRepresentation::id).containsExactly("mmvr-1");
        assertThat(roundTripped.topLevelReference()).extracting(Reference::id)
                .containsExactly("pi-1");
    }

    @Test
    void shouldOmitEmptyEnvelopeMembers() throws Exception {
        Ddi4Response empty = new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, null, null,
                null, null);

        String json = mapper.writeValueAsString(empty);

        assertThat(json).doesNotContain("null").doesNotContain("topLevelReferences");
    }
}
