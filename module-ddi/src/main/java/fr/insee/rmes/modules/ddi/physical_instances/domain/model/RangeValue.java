package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RangeValue(
        @JsonProperty("IsInclusive") Boolean isInclusive,
        @JsonProperty("value") Double value
) {
}
