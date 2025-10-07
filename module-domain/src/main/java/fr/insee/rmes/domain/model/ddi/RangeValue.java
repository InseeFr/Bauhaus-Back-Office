package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RangeValue(
        @JsonProperty("@isInclusive") String isInclusive,
        @JsonProperty("#text") String text
) {
}
