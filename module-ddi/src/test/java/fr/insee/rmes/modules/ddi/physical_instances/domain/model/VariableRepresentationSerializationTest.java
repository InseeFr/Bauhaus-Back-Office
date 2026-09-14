package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Valeurs sentinelles (#1566) : une {@code VariableRepresentation} porte une référence optionnelle
 * {@code MissingValuesReference} vers une {@code ManagedMissingValuesRepresentation}, commune aux
 * quatre types de représentation (Code, Numeric, DateTime, Text).
 */
class VariableRepresentationSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Test
    void missingValuesReference_survivesJacksonRoundTrip() throws Exception {
        String json = """
                {
                    "CodeRepresentation": {
                        "$type": "CodeRepresentationBaseType"
                    },
                    "MissingValuesReference": {
                        "$type": "ManagedMissingValuesRepresentation",
                        "URN": "urn:ddi:fr.insee:mmvr-1:1",
                        "Agency": "fr.insee",
                        "ID": "mmvr-1",
                        "Version": "1"
                    }
                }
                """;

        VariableRepresentation rep = mapper.readValue(json, VariableRepresentation.class);
        String out = mapper.writeValueAsString(rep);

        assertThat(out)
                .contains("\"MissingValuesReference\"")
                .contains("\"urn:ddi:fr.insee:mmvr-1:1\"")
                .contains("\"ManagedMissingValuesRepresentation\"");
    }
}
