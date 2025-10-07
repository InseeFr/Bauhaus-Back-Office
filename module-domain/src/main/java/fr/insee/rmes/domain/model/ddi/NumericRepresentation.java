package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NumericRepresentation(
        @JsonProperty("NumericTypeCode") String numericTypeCode,
        @JsonProperty("NumberRange") NumberRange numberRange
) {
}