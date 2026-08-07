package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DateTimeRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("DateTypeCode") String dateTypeCode,
        @JsonProperty("DateFieldFormat") String dateFieldFormat
) {

    public static final String TYPE = "DateTimeRepresentationBaseType";
}
