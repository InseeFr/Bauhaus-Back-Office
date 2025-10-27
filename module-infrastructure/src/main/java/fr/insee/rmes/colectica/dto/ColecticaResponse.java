package fr.insee.rmes.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ColecticaResponse(
    @JsonProperty("Results")
    List<ColecticaItem> results
) {}