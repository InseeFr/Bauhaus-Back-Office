package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DDI4 StudyUnit domain model.
 * <p>
 * A StudyUnit represents an individual statistical operation.
 * It includes an {@code operationIri} pointing to the RDF URI of the operation,
 * used to generate the {@code <r:UserID>} tag in DDI3 XML.
 */
public record Ddi4StudyUnit(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("@versionDate") String versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Citation") Citation citation,
        String operationIri,
        List<DDIReference> physicalInstanceReferences
) implements Ddi4Item {
}
