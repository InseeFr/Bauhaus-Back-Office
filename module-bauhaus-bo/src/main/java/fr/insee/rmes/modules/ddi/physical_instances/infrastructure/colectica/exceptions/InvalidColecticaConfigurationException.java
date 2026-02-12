package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.exceptions;

/**
 * Exception thrown when the Colectica configuration is invalid.
 * This includes issues such as:
 * - Missing or empty required fields
 * - Invalid format for language codes
 * - Invalid authentication mode
 * - Malformed URLs
 */
public class InvalidColecticaConfigurationException extends RuntimeException {
    public InvalidColecticaConfigurationException(String message) {
        super(message);
    }
}
