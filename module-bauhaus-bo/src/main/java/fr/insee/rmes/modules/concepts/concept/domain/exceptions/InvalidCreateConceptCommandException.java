package fr.insee.rmes.modules.concepts.concept.domain.exceptions;

public class InvalidCreateConceptCommandException extends Throwable {
    public InvalidCreateConceptCommandException(String message) {
        super(message);
    }
}
