package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NumericRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("NumericTypeCode") String numericTypeCode,
        @JsonProperty("NumberRange") NumberRange numberRange
) {

    public static final String TYPE = "NumericRepresentationBaseType";
}
