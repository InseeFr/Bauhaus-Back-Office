package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4CodeList(
        @JsonProperty("$type") String type,
        @JsonProperty("VersionDate") CogsDate versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("BasedOnObject") BasedOnObject basedOnObject,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("Level") List<Level> level,
        @JsonProperty("Code") List<Code> code)
        implements Ddi4VersionedItem {

    public static final String TYPE = "CodeList";

    /**
     * Constructeur de compatibilité (sans {@code BasedOnObject}) : la plupart des listes ne sont
     * pas des variantes — seule une liste forkée porte la référence à sa liste d'origine.
     */
    public Ddi4CodeList(
            String type,
            CogsDate versionDate,
            String urn,
            String agency,
            String id,
            String version,
            List<LangString> label,
            List<Level> level,
            List<Code> code) {
        this(type, versionDate, urn, agency, id, version, null, label, level, code);
    }

    @Override
    public Ddi4CodeList withVersionDate(CogsDate versionDate) {
        return new Ddi4CodeList(type, versionDate, urn, agency, id, version, basedOnObject, label, level, code);
    }
}
