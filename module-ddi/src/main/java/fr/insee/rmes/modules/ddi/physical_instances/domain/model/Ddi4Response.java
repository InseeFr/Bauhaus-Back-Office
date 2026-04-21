package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Ddi4Response(
        @JsonProperty("$schema") String schema,
        List<TopLevelReference> topLevelReference,
        @JsonProperty("PhysicalInstance") List<Ddi4PhysicalInstance> physicalInstance,
        @JsonProperty("DataRelationship") List<Ddi4DataRelationship> dataRelationship,
        @JsonProperty("Variable") List<Ddi4Variable> variable,
        @JsonProperty("CodeList") List<Ddi4CodeList> codeList,
        @JsonProperty("Category") List<Ddi4Category> category
) {
}