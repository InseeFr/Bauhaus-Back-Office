package fr.insee.rmes.keycloak;

public class MissingKeycloakConfigurationException extends RuntimeException {

    public MissingKeycloakConfigurationException() {
        super("Unable to retrieve token: Keycloak configuration is missing or invalid");
    }
}
