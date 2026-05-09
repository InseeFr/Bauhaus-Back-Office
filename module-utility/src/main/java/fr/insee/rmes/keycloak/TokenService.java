package fr.insee.rmes.keycloak;

public interface TokenService {

    String getAccessToken();

    boolean isTokenValid(String token);
}
