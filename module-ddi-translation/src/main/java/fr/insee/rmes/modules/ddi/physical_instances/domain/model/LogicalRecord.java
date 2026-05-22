package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.ddi.physical_instances.generated.LangString;

import java.util.List;

public record LogicalRecord(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") List<LangString> label,
        @JsonProperty("VariablesInRecord") VariablesInRecord variablesInRecord
) {
}