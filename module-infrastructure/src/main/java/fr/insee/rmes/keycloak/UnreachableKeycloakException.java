package fr.insee.rmes.keycloak;

public class UnreachableKeycloakException extends RuntimeException {

    public UnreachableKeycloakException(Throwable cause) {
        super("Keycloak server is unreachable", cause);
    }
}
