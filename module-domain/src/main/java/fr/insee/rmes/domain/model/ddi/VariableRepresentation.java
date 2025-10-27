package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VariableRepresentation(
        @JsonProperty("VariableRole") String variableRole,
        @JsonProperty("CodeRepresentation") CodeRepresentation codeRepresentation,
        @JsonProperty("NumericRepresentation") NumericRepresentation numericRepresentation
) {
}