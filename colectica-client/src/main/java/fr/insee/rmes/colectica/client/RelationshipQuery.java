package fr.insee.rmes.colectica.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request body for the {@code _query/relationship/{direction}/descriptions} endpoints.
 *
 * @param itemTypes Colectica item-type GUIDs to keep (server-side filter). Empty means no filter.
 * @param targetItem the item whose relationships are queried.
 * @param useDistinctTargetItem {@code true} to restrict the query to the version of {@code targetItem}.
 *     Without it, Colectica ignores that version and returns the relationships of every version of
 *     the target. {@code null} (omitted) for an unversioned query.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RelationshipQuery(
        @JsonProperty("itemTypes") List<String> itemTypes,
        @JsonProperty("targetItem") TargetItem targetItem,
        @JsonProperty("useDistinctTargetItem") Boolean useDistinctTargetItem) {

    static RelationshipQuery allVersions(List<String> itemTypes, ItemReference target) {
        return new RelationshipQuery(itemTypes, new TargetItem(target.agencyId(), target.identifier(), null), null);
    }

    static RelationshipQuery ofVersion(List<String> itemTypes, ItemReference target, int version) {
        return new RelationshipQuery(itemTypes, new TargetItem(target.agencyId(), target.identifier(), version), true);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TargetItem(
            @JsonProperty("agencyId") String agencyId,
            @JsonProperty("identifier") String identifier,
            @JsonProperty("version") Integer version) {}
}
