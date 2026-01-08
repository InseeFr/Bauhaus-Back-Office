package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogicalRecordName(
        @JsonProperty("String") StringValue string
) {
}