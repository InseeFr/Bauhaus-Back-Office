package fr.insee.rmes.modules.concepts.concept.domain.exceptions;

public class ConceptAlreadyPublishedException extends Exception {
    public ConceptAlreadyPublishedException(String message) {
        super(message);
    }
}
