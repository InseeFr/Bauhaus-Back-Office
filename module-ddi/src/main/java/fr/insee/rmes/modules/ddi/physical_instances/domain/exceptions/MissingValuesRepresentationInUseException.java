package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

/**
 * Suppression refusée (#1566) : la ManagedMissingValuesRepresentation est encore référencée par au
 * moins une variable. Traduite en 409 {@code {message}} par le contrôleur.
 */
public class MissingValuesRepresentationInUseException extends RuntimeException {

    public MissingValuesRepresentationInUseException(String message) {
        super(message);
    }
}
