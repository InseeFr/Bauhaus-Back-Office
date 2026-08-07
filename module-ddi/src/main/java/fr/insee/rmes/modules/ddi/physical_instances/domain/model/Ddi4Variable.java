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
        @JsonProperty("IsGeographic") Boolean isGeographic
) implements Ddi4VersionedItem {

    public static final String TYPE = "Variable";

    @Override
    public Ddi4Variable withVersionDate(CogsDate versionDate) {
        return new Ddi4Variable(type, versionDate, urn, agency, id, version, basedOnObject,
                variableName, label, description, variableRepresentation, isGeographic);
    }
}