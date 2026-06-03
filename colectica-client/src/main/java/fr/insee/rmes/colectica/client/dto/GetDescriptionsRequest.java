package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GetDescriptionsRequest(
    @JsonProperty("identifiers")
    List<IdentifierRef> identifiers
) {
    public record IdentifierRef(
        @JsonProperty("agencyId")
        String agencyId,
        @JsonProperty("identifier")
        String identifier,
        @JsonProperty("version")
        int version
    ) {}
}
