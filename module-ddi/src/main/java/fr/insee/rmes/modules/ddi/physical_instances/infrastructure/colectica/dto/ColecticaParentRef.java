package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ColecticaParentRef(
    @JsonProperty("AgencyId")
    String agencyId,

    @JsonProperty("Identifier")
    String identifier
) {}
