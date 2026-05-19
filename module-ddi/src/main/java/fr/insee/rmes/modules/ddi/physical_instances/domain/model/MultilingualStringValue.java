package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"LanguageTag", "Value"})
public record MultilingualStringValue(
        @JsonProperty("LanguageTag") String languageTag,
        @JsonProperty("Value") String value
) {
}
