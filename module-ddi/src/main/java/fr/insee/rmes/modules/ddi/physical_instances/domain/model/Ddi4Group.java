package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI4 Group domain model.
 * <p>
 * A Group represents a collection of related StudyUnits (e.g. a statistical operation series).
 * It includes {@code seriesIris} pointing to the RDF URIs of the associated series and a
 * {@code typeOfGroup} describing the nature of the group (e.g. {@code insee:StatisticalOperationSeries}).
 * A group may reference more than one series, and files its non-mutualized code lists under one or
 * more {@link Ddi4LogicalProduct}s pointed to by {@code logicalProductReference}.
 */
public record Ddi4Group(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("VersionResponsibility") String versionResponsibility,
        @JsonProperty("Citation") Citation citation,
        @JsonProperty("StudyUnitReference") List<Reference> studyUnitReference,
        List<String> seriesIris,
        String typeOfGroup,
        @JsonProperty("LogicalProductReference") List<Reference> logicalProductReference)
        implements Ddi4Item {

    public static final String TYPE = "Group";

    /**
     * Backward-compatible constructor for groups that carry no LogicalProductReference.
     */
    public Ddi4Group(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            String versionResponsibility,
            Citation citation,
            List<Reference> studyUnitReference,
            List<String> seriesIris,
            String typeOfGroup) {
        this(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                versionResponsibility,
                citation,
                studyUnitReference,
                seriesIris,
                typeOfGroup,
                null);
    }
}
