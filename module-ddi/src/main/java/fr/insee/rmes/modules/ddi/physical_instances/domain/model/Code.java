package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Code(
        @JsonProperty("$type") String type,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("CategoryReference") Reference categoryReference,
        @JsonProperty("Value") String value
) {

    public static final String TYPE = "CodeType";
}
