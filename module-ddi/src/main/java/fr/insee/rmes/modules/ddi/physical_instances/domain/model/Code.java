package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Code(
        @JsonProperty("$type") String type,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("CategoryReference") Reference categoryReference,
        @JsonProperty("Value") ValueType value,
        @JsonProperty("Code") List<Code> code) {

    public static final String TYPE = "CodeType";
}
