package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Label(
        @JsonProperty("Content") Content content
) {
}

