package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RangeValue(
        @JsonProperty("@isInclusive") String isInclusive,
        @JsonProperty("#text") String text
) {
}
