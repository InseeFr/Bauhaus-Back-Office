package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CodeRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("@blankIsMissingValue") String blankIsMissingValue,
        @JsonProperty("CodeListReference") Reference codeListReference
) {

    public static final String TYPE = "CodeRepresentationBaseType";
}
