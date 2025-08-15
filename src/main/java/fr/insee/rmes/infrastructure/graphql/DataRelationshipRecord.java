package fr.insee.rmes.infrastructure.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DataRelationshipRecord(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("@versionDate") String versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("DataRelationshipName") LangString dataRelationshipName,
        @JsonProperty("LogicalRecord") LogicalRecord logicalRecord
) {
    public record LangString(
            @JsonProperty("String") LangValue string
    ) {}

    public record LangValue(
            @JsonProperty("@xml:lang") String lang,
            @JsonProperty("#text") String text
    ) {}

    public record LogicalRecord(
            @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
            @JsonProperty("URN") String urn,
            @JsonProperty("Agency") String agency,
            @JsonProperty("ID") String id,
            @JsonProperty("Version") String version,
            @JsonProperty("LogicalRecordName") LangString logicalRecordName,
            @JsonProperty("VariablesInRecord") VariablesInRecord variablesInRecord
    ) {}

    public record VariablesInRecord(
            @JsonProperty("VariableUsedReference") List<VariableUsedReference> variableUsedReference
    ) {}

    public record VariableUsedReference(
            @JsonProperty("Agency") String agency,
            @JsonProperty("ID") String id,
            @JsonProperty("Version") String version,
            @JsonProperty("TypeOfObject") String typeOfObject
    ) {}
}