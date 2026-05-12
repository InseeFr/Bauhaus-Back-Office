package fr.insee.rmes.modules.concepts.concept.domain.exceptions;

public class InvalidConceptIdException extends RuntimeException {
    public InvalidConceptIdException(String message) {
        super(message);
    }
}
