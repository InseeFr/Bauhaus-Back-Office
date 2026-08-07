package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4DataRelationship(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("LogicalRecord") List<LogicalRecord> logicalRecord
) implements Ddi4VersionedItem {

    public static final String TYPE = "DataRelationship";

    @Override
    public Ddi4DataRelationship withVersionDate(CogsDate versionDate) {
        return new Ddi4DataRelationship(type, versionDate, urn, agency, id, version,
                basedOnObject, label, logicalRecord);
    }
}