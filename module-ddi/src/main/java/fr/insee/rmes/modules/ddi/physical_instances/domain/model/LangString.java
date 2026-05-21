package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"@language", "@value"})
public record LangString(
        @JsonProperty("@language") String language,
        @JsonProperty("@value") String value
) {
}
