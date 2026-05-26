package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Ddi4DataRelationship(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("LogicalRecord") LogicalRecord logicalRecord
) {

    public static final String TYPE = "DataRelationship";
}