package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request body for {@code item/_updateState} (e.g. deprecating items).
 *
 * @param ids                items to update
 * @param state              new state flag (e.g. {@code true} to deprecate)
 * @param applyToAllVersions whether the state applies to every version of each item
 */
public record UpdateItemStateRequest(
        @JsonProperty("ids") List<ItemIdentifier> ids,
        @JsonProperty("state") boolean state,
        @JsonProperty("applyToAllVersions") boolean applyToAllVersions) {
    public record ItemIdentifier(
            @JsonProperty("agencyId") String agencyId,
            @JsonProperty("identifier") String identifier,
            @JsonProperty("version") int version) {}
}
