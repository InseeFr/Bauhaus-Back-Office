package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DDI4 CategoryScheme domain model.
 * <p>
 * A CategoryScheme is the container under which a group's categories are filed. It references the
 * categories it contains through {@code categoryReference} (DDI 3.3 {@code <CategoryReference>}
 * elements). Like {@link Ddi4CodeListScheme} it is filed under the group's {@link Ddi4LogicalProduct},
 * and carries a {@code Label} rather than a {@code Citation}, so it deliberately does not implement
 * {@link Ddi4Item}.
 */
public record Ddi4CategoryScheme(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("CategoryReference") List<Reference> categoryReference
) {

    public static final String TYPE = "CategoryScheme";
}
