package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticationResponse(
        @JsonProperty("access_token")
        String accessToken
) {
}