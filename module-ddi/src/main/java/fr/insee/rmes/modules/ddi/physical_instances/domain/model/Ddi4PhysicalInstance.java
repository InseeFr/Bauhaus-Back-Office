package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4PhysicalInstance(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Citation") Citation citation,
        @JsonProperty("DataRelationshipReference") List<Reference> dataRelationshipReference
) implements Ddi4VersionedItem {

    public static final String TYPE = "PhysicalInstance";

    @Override
    public Ddi4PhysicalInstance withVersionDate(CogsDate versionDate) {
        return new Ddi4PhysicalInstance(type, versionDate, urn, agency, id, version,
                basedOnObject, citation, dataRelationshipReference);
    }
}