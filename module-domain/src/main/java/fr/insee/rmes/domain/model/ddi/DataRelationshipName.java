package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DataRelationshipName(
        @JsonProperty("String") StringValue string
) {
}