package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4Response(
        @JsonProperty("$schema") String schema,
        @JsonProperty("TopLevelReference") List<Reference> topLevelReference,
        @JsonProperty("PhysicalInstance") List<Ddi4PhysicalInstance> physicalInstance,
        @JsonProperty("DataRelationship") List<Ddi4DataRelationship> dataRelationship,
        @JsonProperty("Variable") List<Ddi4Variable> variable,
        @JsonProperty("CodeList") List<Ddi4CodeList> codeList,
        @JsonProperty("Category") List<Ddi4Category> category
) {
    /** Identifiant du schéma DDI 4 porté par le champ {@code $schema} des réponses. */
    public static final String SCHEMA = "ddi:4.0";
}