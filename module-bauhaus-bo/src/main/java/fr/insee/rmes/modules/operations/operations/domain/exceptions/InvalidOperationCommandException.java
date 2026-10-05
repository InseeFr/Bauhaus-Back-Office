package fr.insee.rmes.modules.operations.operations.domain.exceptions;

/**
 * Invariant d'un {@code OperationCommand} rompu.
 * <p>
 * Non contrôlée : un record valide dans son constructeur compact, où Java interdit de déclarer
 * {@code throws}. Le corps HTTP étant déjà validé par Bean Validation, cette exception ne signale
 * plus qu'un appel programmatique fautif.
 */
public class InvalidOperationCommandException extends RuntimeException {
    public InvalidOperationCommandException(String message) {
        super(message);
    }
}
