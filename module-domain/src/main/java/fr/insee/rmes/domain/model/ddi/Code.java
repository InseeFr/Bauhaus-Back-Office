package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Code(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("CategoryReference") CategoryReference categoryReference,
        @JsonProperty("Value") String value
) {
}