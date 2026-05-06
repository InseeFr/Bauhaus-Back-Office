package fr.insee.rmes.modules.concepts.collections.domain.exceptions;

public class CollectionAlreadyExistsException extends RuntimeException {
    public CollectionAlreadyExistsException(String message) {
        super(message);
    }
}
