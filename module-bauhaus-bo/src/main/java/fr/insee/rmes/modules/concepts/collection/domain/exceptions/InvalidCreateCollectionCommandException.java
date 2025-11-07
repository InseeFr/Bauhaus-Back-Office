package fr.insee.rmes.modules.concepts.collection.domain.exceptions;

public class InvalidCreateCollectionCommandException extends Throwable {
    public InvalidCreateCollectionCommandException(String message) {
        super(message);
    }
}
