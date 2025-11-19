package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QueryRequest(
    @JsonProperty("itemTypes") List<String> itemTypes,
    @JsonProperty("searchLatestVersion") boolean searchLatestVersion
) {
    public QueryRequest(List<String> itemTypes) {
        this(itemTypes, true);
    }
}