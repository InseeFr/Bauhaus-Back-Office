package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI4 VariableScheme domain model.
 * <p>
 * A VariableScheme is the container under which a study unit's variables are filed. It references
 * the variables it contains through {@code variableReference} (DDI 3.3 {@code <VariableReference>}
 * elements). Unlike the group's schemes it is filed under the {@link Ddi4StudyUnit}'s
 * {@link Ddi4LogicalProduct} (one per series). Like {@link Ddi4CodeListScheme} it carries a
 * {@code Label} rather than a {@code Citation}, so it deliberately does not implement {@link Ddi4Item}.
 */
public record Ddi4VariableScheme(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("VariableReference") List<Reference> variableReference) {

    public static final String TYPE = "VariableScheme";
}
