package fr.insee.rmes.modules.operations.series.domain.exceptions;

/**
 * Invariant d'un {@code SeriesCommand} rompu.
 * <p>
 * Non contrôlée : un record valide dans son constructeur compact, où Java interdit de déclarer
 * {@code throws}. Le corps HTTP étant déjà validé par Bean Validation, cette exception ne signale
 * plus qu'un appel programmatique fautif.
 */
public class InvalidSeriesCommandException extends RuntimeException {
    public InvalidSeriesCommandException(String message) {
        super(message);
    }
}
