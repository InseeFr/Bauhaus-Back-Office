package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Content(
        @JsonProperty("@xml:lang") String xmlLang,
        @JsonProperty("#text") String text
) {
}
