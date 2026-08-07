package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("MaxLength") Integer maxLength,
        @JsonProperty("MinLength") Integer minLength,
        @JsonProperty("RegExp") String regExp,
        @JsonProperty("BlankIsMissingValue") Boolean blankIsMissingValue
) {

    public static final String TYPE = "TextRepresentationBaseType";
}
