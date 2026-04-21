package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

public record AuthenticationRequest(
        String username,
        String password
) {
}