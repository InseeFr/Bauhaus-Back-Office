package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Ddi4Category(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label
) implements Ddi4VersionedItem {

    public static final String TYPE = "Category";

    @Override
    public Ddi4Category withVersionDate(CogsDate versionDate) {
        return new Ddi4Category(type, versionDate, urn, agency, id, version, label);
    }
}