package fr.insee.rmes.modules.organisations;

/**
 * Signale que la propriété {@code fr.insee.rmes.bauhaus.defaultContributor} est
 * inexploitable : valeur vide, IRI syntaxiquement invalide, organisation absente de la base de
 * gestion, ou base de gestion incapable de répondre.
 * <p>
 * Levée pendant l'initialisation du contexte Spring : l'application ne démarre pas.
 */
public class InvalidDefaultContributorException extends IllegalStateException {

    public InvalidDefaultContributorException(String message) {
        super(message);
    }

    public InvalidDefaultContributorException(String message, Throwable cause) {
        super(message, cause);
    }
}
