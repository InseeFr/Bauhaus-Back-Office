package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI4 ManagedRepresentationScheme domain model.
 * <p>
 * A ManagedRepresentationScheme is the container under which a group's managed representations
 * (managed text, numeric, date-time… representations) are filed. It references the representations
 * it contains through {@code managedRepresentationReference} (DDI 3.3
 * {@code <ManagedRepresentationReference>} elements). Like {@link Ddi4CodeListScheme} and
 * {@link Ddi4CategoryScheme} it is filed under the group's {@link Ddi4LogicalProduct}, and carries
 * a {@code Label} rather than a {@code Citation}, so it deliberately does not implement
 * {@link Ddi4Item}.
 */
public record Ddi4ManagedRepresentationScheme(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("ManagedRepresentationReference") List<Reference> managedRepresentationReference) {

    public static final String TYPE = "ManagedRepresentationScheme";
}
