package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NumberRange(
        @JsonProperty("Low") RangeValue low,
        @JsonProperty("High") RangeValue high
) {
}

