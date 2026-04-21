package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ColecticaResponse(
    @JsonProperty("Results")
    List<ColecticaItem> results,

    @JsonProperty("TotalResults")
    int totalResults,

    @JsonProperty("ReturnedResults")
    int returnedResults,

    @JsonProperty("NextResult")
    String nextResult,

    @JsonProperty("DatabaseTime")
    String databaseTime,

    @JsonProperty("RepositoryTime")
    String repositoryTime
) {}