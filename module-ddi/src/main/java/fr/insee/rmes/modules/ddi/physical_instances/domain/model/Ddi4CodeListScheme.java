package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI4 CodeListScheme domain model.
 * <p>
 * A CodeListScheme is the container under which a group's non-mutualized code lists are
 * filed. It references the code lists it contains through {@code codeListReference}
 * (DDI 3.3 {@code <CodeListReference>} elements). Unlike most DDI items it carries a
 * {@code Label} rather than a {@code Citation}, so it deliberately does not implement
 * {@link Ddi4Item}.
 */
public record Ddi4CodeListScheme(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("CodeListReference") List<Reference> codeListReference) {

    public static final String TYPE = "CodeListScheme";
}
