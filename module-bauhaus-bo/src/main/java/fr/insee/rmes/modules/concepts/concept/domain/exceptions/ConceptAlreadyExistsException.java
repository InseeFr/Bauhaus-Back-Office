package fr.insee.rmes.modules.concepts.concept.domain.exceptions;

public class ConceptAlreadyExistsException extends RuntimeException {
    public ConceptAlreadyExistsException(String message) {
        super(message);
    }
}
