package fr.insee.rmes.modules.concepts.collections.domain.exceptions;

public class InvalidUpdateCollectionCommandException extends RuntimeException {
    public InvalidUpdateCollectionCommandException(String message) {
        super(message);
    }
}
