package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record VariablesInRecord(
        @JsonProperty("VariableUsedReference") List<VariableUsedReference> variableUsedReference
) {
}