package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VariableRepresentation(
        @JsonProperty("VariableRole") String variableRole,
        @JsonProperty("CodeRepresentation") CodeRepresentation codeRepresentation,
        @JsonProperty("NumericRepresentation") NumericRepresentation numericRepresentation,
        @JsonProperty("DateTimeRepresentation") DateTimeRepresentation dateTimeRepresentation,
        @JsonProperty("TextRepresentation") TextRepresentation textRepresentation,
        @JsonProperty("MissingValuesReference") Reference missingValuesReference
) {
}