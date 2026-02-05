package fr.insee.rmes.modules.concepts.collections.domain.exceptions;

public class InvalidCollectionIdException extends RuntimeException {
    public InvalidCollectionIdException(String message) {
        super(message);
    }
}
