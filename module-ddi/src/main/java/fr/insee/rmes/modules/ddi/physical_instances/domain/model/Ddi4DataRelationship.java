package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4DataRelationship(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("LogicalRecord") List<LogicalRecord> logicalRecord,
        @JsonProperty("VersionResponsibility") String versionResponsibility)
        implements Ddi4VersionedItem {

    public static final String TYPE = "DataRelationship";

    /**
     * Constructeur de compatibilité (sans {@code VersionResponsibility}) : le champ est estampillé
     * à l'écriture depuis {@code colectica.yml}, les constructions internes ne le renseignent pas.
     */
    public Ddi4DataRelationship(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            BasedOnObject basedOnObject,
            List<LangString> label,
            List<LogicalRecord> logicalRecord) {
        this(type, versionDate, urn, agency, id, version, basedOnObject, label, logicalRecord, null);
    }

    @Override
    public Ddi4DataRelationship withVersionDate(CogsDate versionDate) {
        return new Ddi4DataRelationship(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                basedOnObject,
                label,
                logicalRecord,
                versionResponsibility);
    }

    @Override
    public Ddi4DataRelationship withVersionResponsibility(String versionResponsibility) {
        return new Ddi4DataRelationship(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                basedOnObject,
                label,
                logicalRecord,
                versionResponsibility);
    }
}
