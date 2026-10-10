package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Items and assertions shared by the wire-format tests of the DDI 4 envelopes. */
final class Ddi4WireFormatFixtures {

    private Ddi4WireFormatFixtures() {}

    /** The serialized envelope carries exactly the two root properties of the schema. */
    static void assertSerializesToTheSchemaEnvelope(ObjectMapper mapper, Object response)
            throws JsonProcessingException {
        JsonNode json = mapper.readTree(mapper.writeValueAsString(response));

        assertThat(json.fieldNames()).toIterable().containsExactlyInAnyOrder("topLevelReferences", "items");
    }

    /** The physical instance {@code fr.insee/pi-1}, titled "Ma PI". */
    static Ddi4PhysicalInstance aPhysicalInstance() {
        return new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                null,
                "urn:ddi:fr.insee:pi-1:1",
                "fr.insee",
                "pi-1",
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Ma PI")),
                null);
    }
}
