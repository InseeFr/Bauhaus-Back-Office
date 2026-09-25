package fr.insee.rmes.modules.operations.indicators.domain.exceptions;

/**
 * Invariant d'un {@code IndicatorCommand} rompu.
 * <p>
 * Non contrôlée, à la différence des exceptions de domaine des collections : un record valide dans
 * son constructeur compact, où Java interdit de déclarer {@code throws}. Le corps HTTP étant déjà
 * validé par Bean Validation, cette exception ne signale plus qu'un appel programmatique fautif.
 */
public class InvalidIndicatorCommandException extends RuntimeException {
    public InvalidIndicatorCommandException(String message) {
        super(message);
    }
}
