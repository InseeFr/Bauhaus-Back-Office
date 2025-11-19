package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ColecticaCreateItemRequest(
    @JsonProperty("Items") List<ColecticaItemResponse> items,
    @JsonProperty("options") ColecticaItemOptions options
) {
    public ColecticaCreateItemRequest(List<ColecticaItemResponse> items) {
        this(items, ColecticaItemOptions.registerOrReplace());
    }
}