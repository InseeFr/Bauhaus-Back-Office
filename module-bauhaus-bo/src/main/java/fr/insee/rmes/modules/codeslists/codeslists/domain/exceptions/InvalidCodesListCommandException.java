package fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions;

/**
 * Garde de dernier recours du domaine. Le contrat HTTP est tenu en amont par Bean Validation
 * ({@code CodesListRequest}) : cette exception ne devrait remonter que si un autre appelant que
 * le contrôleur construit une commande incomplète.
 */
public class InvalidCodesListCommandException extends RuntimeException {
    public InvalidCodesListCommandException(String message) {
        super(message);
    }
}
