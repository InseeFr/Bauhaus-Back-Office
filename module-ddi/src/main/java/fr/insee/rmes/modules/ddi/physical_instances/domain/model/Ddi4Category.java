package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Ddi4Category(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Label") List<LangString> label
) implements Ddi4VersionedItem {

    public static final String TYPE = "Category";

    /**
     * Constructeur de compatibilité (sans {@code BasedOnObject}) : seule une catégorie forkée en
     * variante porte la référence à la catégorie d'origine.
     */
    public Ddi4Category(String type, CogsDate versionDate, String urn, String agency, String id,
            String version, List<LangString> label) {
        this(type, versionDate, urn, agency, id, version, null, label);
    }

    @Override
    public Ddi4Category withVersionDate(CogsDate versionDate) {
        return new Ddi4Category(type, versionDate, urn, agency, id, version, basedOnObject, label);
    }
}