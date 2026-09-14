package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4Variable(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("VariableName") List<LangString> variableName,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("Description") List<LangString> description,
        @JsonProperty("VariableRepresentation") VariableRepresentation variableRepresentation,
        @JsonProperty("IsGeographic") Boolean isGeographic,
        @JsonProperty("VersionResponsibility") String versionResponsibility)
        implements Ddi4VersionedItem {

    public static final String TYPE = "Variable";

    /**
     * Constructeur de compatibilité (sans {@code VersionResponsibility}) : le champ est estampillé
     * à l'écriture depuis {@code colectica.yml}, les constructions internes ne le renseignent pas.
     */
    public Ddi4Variable(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            BasedOnObject basedOnObject,
            List<LangString> variableName,
            List<LangString> label,
            List<LangString> description,
            VariableRepresentation variableRepresentation,
            Boolean isGeographic) {
        this(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                basedOnObject,
                variableName,
                label,
                description,
                variableRepresentation,
                isGeographic,
                null);
    }

    @Override
    public Ddi4Variable withVersionDate(CogsDate versionDate) {
        return new Ddi4Variable(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                basedOnObject,
                variableName,
                label,
                description,
                variableRepresentation,
                isGeographic,
                versionResponsibility);
    }

    @Override
    public Ddi4Variable withVersionResponsibility(String versionResponsibility) {
        return new Ddi4Variable(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                basedOnObject,
                variableName,
                label,
                description,
                variableRepresentation,
                isGeographic,
                versionResponsibility);
    }
}
