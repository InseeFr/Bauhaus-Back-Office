package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LogicalRecord(
        @JsonProperty("$type") String type,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("VariablesInRecord") VariablesInRecord variablesInRecord
) {

    public static final String TYPE = "LogicalRecord";
}