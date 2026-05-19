package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Title(
        @JsonProperty("String") List<MultilingualStringEntry> strings
) {
}
