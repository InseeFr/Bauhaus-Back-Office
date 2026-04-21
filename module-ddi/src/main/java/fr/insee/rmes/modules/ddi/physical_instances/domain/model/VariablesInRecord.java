package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record VariablesInRecord(
        @JsonProperty("VariableUsedReference") List<VariableUsedReference> variableUsedReference
) {
}