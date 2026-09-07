package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CodeRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("BlankIsMissingValue") Boolean blankIsMissingValue,
        @JsonProperty("CodeListReference") Reference codeListReference
) {

    public static final String TYPE = "CodeRepresentationBaseType";
}
