package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4Category(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("VersionResponsibility") String versionResponsibility)
        implements Ddi4VersionedItem {

    public static final String TYPE = "Category";

    /**
     * Constructeur de compatibilité (sans {@code VersionResponsibility}) : le champ est estampillé
     * à l'écriture depuis {@code colectica.yml}, les constructions internes ne le renseignent pas.
     */
    public Ddi4Category(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            BasedOnObject basedOnObject,
            List<LangString> label) {
        this(type, versionDate, urn, agency, id, version, basedOnObject, label, null);
    }

    /**
     * Constructeur de compatibilité (sans {@code BasedOnObject}) : seule une catégorie forkée en
     * variante porte la référence à la catégorie d'origine.
     */
    public Ddi4Category(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            List<LangString> label) {
        this(type, versionDate, urn, agency, id, version, null, label);
    }

    @Override
    public Ddi4Category withVersionDate(CogsDate versionDate) {
        return new Ddi4Category(
                type, versionDate, urn, agency, id, version, basedOnObject, label, versionResponsibility);
    }

    @Override
    public Ddi4Category withVersionResponsibility(String versionResponsibility) {
        return new Ddi4Category(
                type, versionDate, urn, agency, id, version, basedOnObject, label, versionResponsibility);
    }
}
