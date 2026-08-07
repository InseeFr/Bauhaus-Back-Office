package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RangeValue(
        @JsonProperty("IsInclusive") Boolean isInclusive,
        @JsonProperty("value") Double value
) {
}
