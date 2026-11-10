package fr.insee.rmes.graphdb;

import java.util.Optional;

/**
 * Identifiants envoyés en authentification basique à Fuseki, lus depuis {@value #USERNAME_PROPERTY} et
 * {@value #PASSWORD_PROPERTY}. Sans eux, les requêtes partent sans authentification.
 */
public record RdfBasicCredentials(String username, String password) {

    public static final String USERNAME_PROPERTY = "fr.insee.rmes.rdf.basic-auth.username";

    public static final String PASSWORD_PROPERTY = "fr.insee.rmes.rdf.basic-auth.password";

    public static Optional<RdfBasicCredentials> fromProperties(String username, String password) {
        boolean withUsername = username != null && !username.isBlank();
        boolean withPassword = password != null && !password.isBlank();
        if (withUsername != withPassword) {
            throw new IllegalArgumentException("%s et %s vont ensemble : renseigner les deux, ou aucun."
                    .formatted(USERNAME_PROPERTY, PASSWORD_PROPERTY));
        }
        return withUsername ? Optional.of(new RdfBasicCredentials(username, password)) : Optional.empty();
    }
}
