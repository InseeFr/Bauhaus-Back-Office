package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Level(
        @JsonProperty("$type") String type,
        @JsonProperty("LevelNumber") Integer levelNumber,
        @JsonProperty("LevelName") List<LangString> levelName,
        @JsonProperty("CategoryRelationship") String categoryRelationship) {

    public static final String TYPE = "LevelType";
}
