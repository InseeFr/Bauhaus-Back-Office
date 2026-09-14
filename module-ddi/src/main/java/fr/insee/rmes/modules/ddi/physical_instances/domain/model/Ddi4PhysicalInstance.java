package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4PhysicalInstance(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Citation") Citation citation,
        @JsonProperty("DataRelationshipReference") List<Reference> dataRelationshipReference,
        @JsonProperty("VersionResponsibility") String versionResponsibility)
        implements Ddi4VersionedItem, Ddi4Item {

    public static final String TYPE = "PhysicalInstance";

    /**
     * Constructeur de compatibilité (sans {@code VersionResponsibility}) : le champ est estampillé
     * à l'écriture depuis {@code colectica.yml}, les constructions internes ne le renseignent pas.
     */
    public Ddi4PhysicalInstance(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            BasedOnObject basedOnObject,
            Citation citation,
            List<Reference> dataRelationshipReference) {
        this(type, versionDate, urn, agency, id, version, basedOnObject, citation, dataRelationshipReference, null);
    }

    @Override
    public Ddi4PhysicalInstance withVersionDate(CogsDate versionDate) {
        return new Ddi4PhysicalInstance(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                basedOnObject,
                citation,
                dataRelationshipReference,
                versionResponsibility);
    }

    @Override
    public Ddi4PhysicalInstance withVersionResponsibility(String versionResponsibility) {
        return new Ddi4PhysicalInstance(
                type,
                versionDate,
                urn,
                agency,
                id,
                version,
                basedOnObject,
                citation,
                dataRelationshipReference,
                versionResponsibility);
    }
}
