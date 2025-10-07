package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VariableName(
        @JsonProperty("String") StringValue string
) {
}