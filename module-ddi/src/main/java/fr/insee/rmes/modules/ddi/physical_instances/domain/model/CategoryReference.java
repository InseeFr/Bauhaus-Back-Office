package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoryReference(
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("TypeOfObject") String typeOfObject
) {
}