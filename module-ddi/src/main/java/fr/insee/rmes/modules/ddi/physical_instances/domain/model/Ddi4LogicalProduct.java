package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI4 LogicalProduct domain model.
 * <p>
 * A LogicalProduct is a container that files the schemes it references. Two kinds are used here:
 * the group's LogicalProduct (filed under a {@link Ddi4Group}) holds the group's
 * {@link Ddi4CodeListScheme}s and {@link Ddi4CategoryScheme}s, while the study unit's
 * LogicalProduct (one per series, filed under a {@link Ddi4StudyUnit}) holds the series'
 * {@link Ddi4VariableScheme}. It references the schemes it contains through
 * {@code codeListSchemeReference}, {@code categorySchemeReference}, {@code variableSchemeReference}
 * and {@code managedRepresentationSchemeReference} (DDI 3.3 {@code <…SchemeReference>} elements). Like
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
        @JsonProperty("CodeListSchemeReference") List<Reference> codeListSchemeReference,
        @JsonProperty("CategorySchemeReference") List<Reference> categorySchemeReference,
        @JsonProperty("VariableSchemeReference") List<Reference> variableSchemeReference,

        @JsonProperty("ManagedRepresentationSchemeReference")
        List<Reference> managedRepresentationSchemeReference) {

    public static final String TYPE = "LogicalProduct";

    /**
     * Backward-compatible constructor for a LogicalProduct that only files a CodeListScheme
     * (no CategoryScheme, no VariableScheme, no ManagedRepresentationScheme).
     */
    public Ddi4LogicalProduct(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            List<LangString> label,
            List<Reference> codeListSchemeReference) {
        this(type, versionDate, urn, agency, id, version, label, codeListSchemeReference, null, null, null);
    }

    /**
     * Backward-compatible constructor for a LogicalProduct without a ManagedRepresentationScheme.
     */
    public Ddi4LogicalProduct(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            List<LangString> label,
            List<Reference> codeListSchemeReference,
            List<Reference> categorySchemeReference,
            List<Reference> variableSchemeReference) {
        this(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                label,
                codeListSchemeReference,
                categorySchemeReference,
                variableSchemeReference,
                null);
    }
}
