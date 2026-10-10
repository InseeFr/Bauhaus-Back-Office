package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Body of {@code POST _query}. {@code searchSets} restricts the search to the items of the sets rooted
 * at the given items; with {@code maxResults}, it is omitted when absent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QueryRequest(
        @JsonProperty("itemTypes") List<String> itemTypes,
        @JsonProperty("searchLatestVersion") boolean searchLatestVersion,
        @JsonProperty("searchSets") List<SearchSet> searchSets,
        @JsonProperty("maxResults") Integer maxResults) {
    public QueryRequest(List<String> itemTypes) {
        this(itemTypes, true, null, null);
    }

    /** Root item of a set to search in. */
    public record SearchSet(
            @JsonProperty("agencyId") String agencyId,
            @JsonProperty("identifier") String identifier,
            @JsonProperty("version") int version) {}
}
