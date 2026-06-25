package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DDI4 LogicalProduct domain model.
 * <p>
 * A LogicalProduct is the container, filed under a {@link Ddi4Group}, that holds the group's
 * {@link Ddi4CodeListScheme}s. It references the schemes it contains through
 * {@code codeListSchemeReference} (DDI 3.3 {@code <CodeListSchemeReference>} elements). Like
 * {@link Ddi4CodeListScheme}, it carries a {@code Label} rather than a {@code Citation}, so it
 * deliberately does not implement {@link Ddi4Item}.
 */
public record Ddi4LogicalProduct(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("CodeListSchemeReference") List<Reference> codeListSchemeReference
) {

    public static final String TYPE = "LogicalProduct";
}
