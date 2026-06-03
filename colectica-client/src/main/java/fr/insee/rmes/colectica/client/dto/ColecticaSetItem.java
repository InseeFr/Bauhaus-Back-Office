package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ColecticaSetItem(
    @JsonProperty("Item1") String identifier,
    @JsonProperty("Item2") int version,
    @JsonProperty("Item3") String agencyId
) {}
