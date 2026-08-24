package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

/**
 * Suppression impossible (#1566) : aucune ManagedMissingValuesRepresentation ne porte cet
 * identifiant. Traduite en 404 par le contrôleur.
 */
public class MissingValuesRepresentationNotFoundException extends RuntimeException {

    public MissingValuesRepresentationNotFoundException(String message) {
        super(message);
    }
}
