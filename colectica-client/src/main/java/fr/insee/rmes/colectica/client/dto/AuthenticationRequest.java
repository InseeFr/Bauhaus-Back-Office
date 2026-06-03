package fr.insee.rmes.colectica.client.dto;

public record AuthenticationRequest(
        String username,
        String password
) {
}
