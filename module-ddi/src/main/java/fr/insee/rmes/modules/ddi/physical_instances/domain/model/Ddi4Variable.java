package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Ddi4Variable(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("VariableName") List<LangString> variableName,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("Description") List<LangString> description,
        @JsonProperty("VariableRepresentation") VariableRepresentation variableRepresentation,
        @JsonProperty("@isGeographic") String isGeographic
) {

    public static final String TYPE = "Variable";
}