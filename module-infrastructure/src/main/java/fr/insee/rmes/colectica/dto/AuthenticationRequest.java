package fr.insee.rmes.colectica.dto;

public record AuthenticationRequest(
        String username,
        String password
) {
}