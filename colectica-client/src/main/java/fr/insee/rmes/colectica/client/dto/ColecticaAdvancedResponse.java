package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response envelope of {@code POST _query/advanced}. The advanced endpoint paginates with
 * {@code NextResult} (a cursor token, {@code null} when exhausted) and does not return a
 * {@code TotalResults} count.
 */
public record ColecticaAdvancedResponse(
    @JsonProperty("Results")
    List<ColecticaAdvancedItem> results,

    @JsonProperty("ReturnedResults")
    int returnedResults,

    @JsonProperty("NextResult")
    String nextResult
) {}
