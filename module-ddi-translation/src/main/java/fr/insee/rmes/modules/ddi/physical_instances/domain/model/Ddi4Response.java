package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Category;
import fr.insee.rmes.modules.ddi.physical_instances.generated.CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.generated.DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.generated.PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Variable;

import java.util.List;

public record Ddi4Response(
        @JsonProperty("$schema") String schema,
        List<TopLevelReference> topLevelReference,
        @JsonProperty("PhysicalInstance") List<PhysicalInstance> physicalInstance,
        @JsonProperty("DataRelationship") List<DataRelationship> dataRelationship,
        @JsonProperty("Variable") List<Variable> variable,
        @JsonProperty("CodeList") List<CodeList> codeList,
        @JsonProperty("Category") List<Category> category
) {
}