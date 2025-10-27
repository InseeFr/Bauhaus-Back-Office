package fr.insee.rmes.domain.model.ddi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogicalRecord(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("LogicalRecordName") LogicalRecordName logicalRecordName,
        @JsonProperty("VariablesInRecord") VariablesInRecord variablesInRecord
) {
}