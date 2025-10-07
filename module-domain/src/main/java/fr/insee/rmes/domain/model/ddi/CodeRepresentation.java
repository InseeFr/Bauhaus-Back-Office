package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CodeRepresentation(
        @JsonProperty("@blankIsMissingValue") String blankIsMissingValue,
        @JsonProperty("CodeListReference") CodeListReference codeListReference
) {
}