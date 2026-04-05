package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DDI4 Group domain model.
 * <p>
 * A Group represents a collection of related StudyUnits (e.g. a statistical operation series).
 * It includes a {@code seriesIri} pointing to the RDF URI of the series and a {@code typeOfGroup}
 * describing the nature of the group (e.g. {@code insee:StatisticalOperationSeries}).
 */
public record Ddi4Group(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("@versionDate") String versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("VersionResponsibility") String versionResponsibility,
        @JsonProperty("Citation") Citation citation,
        @JsonProperty("StudyUnitReference") List<StudyUnitReference> studyUnitReference,
        String seriesIri,
        String typeOfGroup
) implements Ddi4Item {
}
