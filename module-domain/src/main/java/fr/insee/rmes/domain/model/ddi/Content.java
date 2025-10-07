package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Content(
        @JsonProperty("@xml:lang") String xmlLang,
        @JsonProperty("#text") String text
) {
}
