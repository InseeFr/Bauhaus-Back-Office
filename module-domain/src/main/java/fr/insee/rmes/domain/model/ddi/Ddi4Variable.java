package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Ddi4Variable(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("@versionDate") String versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("VariableName") VariableName variableName,
        @JsonProperty("Label") Label label,
        @JsonProperty("Description") Description description,
        @JsonProperty("VariableRepresentation") VariableRepresentation variableRepresentation,
        @JsonProperty("@isGeographic") String isGeographic
) {
}