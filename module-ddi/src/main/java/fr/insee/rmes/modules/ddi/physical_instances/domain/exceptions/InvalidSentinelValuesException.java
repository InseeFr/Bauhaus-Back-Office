package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

/**
 * Payload de sauvegarde invalide côté valeurs sentinelles (#1566) : une
 * ManagedMissingValuesRepresentation ou sa CodeList de sentinelles sans label — la carte les rend
 * obligatoires. Traduite en 400 {@code {message}} par le {@code DdiExceptionHandler}.
 */
public class InvalidSentinelValuesException extends RuntimeException {

    public InvalidSentinelValuesException(String message) {
        super(message);
    }
}
