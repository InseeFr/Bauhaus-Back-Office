package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RelationshipBySubjectRequest(
    @JsonProperty("itemTypes") List<String> itemTypes,
    @JsonProperty("targetItem") TargetItemRef targetItem
) {
    public record TargetItemRef(
        @JsonProperty("agencyId") String agencyId,
        @JsonProperty("identifier") String identifier
    ) {}
}
