package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI4 StudyUnit domain model.
 * <p>
 * A StudyUnit represents an individual statistical operation.
 * It includes an {@code operationIri} pointing to the RDF URI of the operation,
 * used to generate the {@code <r:UserID>} tag in DDI3 XML. It also files its series'
 * {@link Ddi4LogicalProduct} (which holds the {@link Ddi4VariableScheme}) through
 * {@code logicalProductReferences} (DDI 3.3 {@code <LogicalProductReference>} elements).
 */
public record Ddi4StudyUnit(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Citation") Citation citation,
        String operationIri,
        @JsonProperty("PhysicalInstanceReference") List<Reference> physicalInstanceReferences,
        @JsonProperty("LogicalProductReference") List<Reference> logicalProductReferences)
        implements Ddi4Item {

    public static final String TYPE = "StudyUnit";

    /**
     * Backward-compatible constructor for a StudyUnit that files no LogicalProduct.
     */
    public Ddi4StudyUnit(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            Citation citation,
            String operationIri,
            List<Reference> physicalInstanceReferences) {
        this(type, versionDate, urn, agency, id, version, citation, operationIri, physicalInstanceReferences, null);
    }
}
