package fr.insee.rmes.colectica.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request body for the {@code _query/relationship/{direction}/descriptions} endpoints.
 *
 * @param itemTypes Colectica item-type GUIDs to keep (server-side filter). Empty means no filter.
 * @param targetItem the item whose relationships are queried.
 */
public record RelationshipQuery(
    @JsonProperty("itemTypes") List<String> itemTypes,
    @JsonProperty("targetItem") TargetItem targetItem
) {
    public record TargetItem(
        @JsonProperty("agencyId") String agencyId,
        @JsonProperty("identifier") String identifier
    ) {}
}
