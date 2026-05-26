package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NumericRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("NumericTypeCode") String numericTypeCode,
        @JsonProperty("NumberRange") NumberRange numberRange
) {

    public static final String TYPE = "NumericRepresentationBaseType";
}
