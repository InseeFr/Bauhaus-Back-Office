package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NumberRange(
        @JsonProperty("Low") RangeValue low,
        @JsonProperty("High") RangeValue high
) {
}

