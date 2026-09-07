package fr.insee.rmes.modules.concepts.collections.domain.exceptions;

public class CollectionAlreadyPublishedException extends Exception {
    public CollectionAlreadyPublishedException(String message) {
        super(message);
    }
}
