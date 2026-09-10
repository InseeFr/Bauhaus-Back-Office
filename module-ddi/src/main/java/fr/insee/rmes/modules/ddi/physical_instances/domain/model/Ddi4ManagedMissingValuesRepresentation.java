package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI4 ManagedMissingValuesRepresentation domain model (valeurs sentinelles, cf. #1566).
 * <p>
 * A reusable description of the missing values shared by several variables, filed under a group's
 * {@link Ddi4ManagedRepresentationScheme} (referenced through its
 * {@code ManagedRepresentationReference}). The V1 scope only carries a {@code Label} and
 * {@code MissingCodeRepresentation} entries — in-line {@link CodeRepresentation}s whose
 * {@code CodeListReference} points to the CodeList holding the sentinel codes (filed in the same
 * group's CodeListScheme as the expected codes). The other {@code Missing*Representation} kinds
 * (numeric, text) are out of scope for now.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4ManagedMissingValuesRepresentation(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("MissingCodeRepresentation") List<CodeRepresentation> missingCodeRepresentation,
        @JsonProperty("VersionResponsibility") String versionResponsibility)
        implements Ddi4VersionedItem {

    public static final String TYPE = "ManagedMissingValuesRepresentation";

    /**
     * Constructeur de compatibilité (sans {@code VersionResponsibility}) : le champ est estampillé
     * à l'écriture depuis {@code colectica.yml}, les constructions internes ne le renseignent pas.
     */
    public Ddi4ManagedMissingValuesRepresentation(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            List<LangString> label,
            List<CodeRepresentation> missingCodeRepresentation) {
        this(type, versionDate, urn, agency, id, version, label, missingCodeRepresentation, null);
    }

    @Override
    public Ddi4ManagedMissingValuesRepresentation withVersionDate(CogsDate versionDate) {
        return new Ddi4ManagedMissingValuesRepresentation(
                type, versionDate, urn, agency, id, version, label, missingCodeRepresentation, versionResponsibility);
    }

    @Override
    public Ddi4ManagedMissingValuesRepresentation withVersionResponsibility(String versionResponsibility) {
        return new Ddi4ManagedMissingValuesRepresentation(
                type, versionDate, urn, agency, id, version, label, missingCodeRepresentation, versionResponsibility);
    }
}
