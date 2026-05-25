package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DDI4 Group domain model.
 * <p>
 * A Group represents a collection of related StudyUnits (e.g. a statistical operation series).
 * It includes {@code seriesIris} pointing to the RDF URIs of the associated series and a
 * {@code typeOfGroup} describing the nature of the group (e.g. {@code insee:StatisticalOperationSeries}).
 * A group may reference more than one series.
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
        List<String> seriesIris,
        String typeOfGroup
) implements Ddi4Item {
}
