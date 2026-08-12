package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

/**
 * Le document soumis à la validation n'est pas du JSON bien formé : la faute est côté appelant, et
 * elle est distincte d'une panne de chargement du schéma, qui elle relève du 500.
 */
public class InvalidDdi4JsonException extends RuntimeException {

    public InvalidDdi4JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
