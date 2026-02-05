package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DateTimeRepresentation(
        @JsonProperty("DateTypeCode") String dateTypeCode,
        @JsonProperty("DateFieldFormat") String dateFieldFormat
) {
}