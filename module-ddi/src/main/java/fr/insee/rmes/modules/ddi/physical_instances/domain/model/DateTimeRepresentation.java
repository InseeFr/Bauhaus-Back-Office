package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DateTimeRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("DateTypeCode") String dateTypeCode,
        @JsonProperty("DateFieldFormat") String dateFieldFormat
) {

    public static final String TYPE = "DateTimeRepresentationBaseType";
}
