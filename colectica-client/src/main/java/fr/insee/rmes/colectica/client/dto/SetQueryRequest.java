package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Body of {@code POST _query/set}: walks the set of {@code rootItem}, keeping only {@code itemTypes}. */
public record SetQueryRequest(
        @JsonProperty("RootItem") RootItem rootItem,
        @JsonProperty("Facet") Facet facet) {

    public record RootItem(
            @JsonProperty("AgencyId") String agencyId,
            @JsonProperty("Identifier") String identifier,
            @JsonProperty("Version") int version) {}

    public record Facet(
            @JsonProperty("ItemTypes") List<String> itemTypes,
            @JsonProperty("ReverseTraversal") boolean reverseTraversal,
            @JsonProperty("UseDistinctResultItem") boolean useDistinctResultItem,
            @JsonProperty("UseDistinctTargetItem") boolean useDistinctTargetItem) {}
}
