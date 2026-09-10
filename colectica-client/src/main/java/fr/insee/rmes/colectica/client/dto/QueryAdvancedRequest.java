package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Body of {@code POST _query/advanced}. Compared to {@link QueryRequest}, asking for
 * {@code resultsIncludeAll} makes Colectica return the rich per-item property bags
 * ({@code TextProperties}/{@code DateProperties}/...), notably {@code DateProperties.versionDate}.
 */
public record QueryAdvancedRequest(
        @JsonProperty("itemTypes") List<String> itemTypes,
        @JsonProperty("searchLatestVersion") boolean searchLatestVersion,
        @JsonProperty("resultsIncludeAll") boolean resultsIncludeAll) {
    public QueryAdvancedRequest(List<String> itemTypes) {
        this(itemTypes, true, true);
    }
}
