package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TextRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("@maxLength") Integer maxLength,
        @JsonProperty("@minLength") Integer minLength,
        @JsonProperty("@regExp") String regExp,
        @JsonProperty("@blankIsMissingValue") String blankIsMissingValue
) {

    public static final String TYPE = "TextRepresentationBaseType";
}
