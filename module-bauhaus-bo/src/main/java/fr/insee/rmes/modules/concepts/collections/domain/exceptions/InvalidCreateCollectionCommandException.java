package fr.insee.rmes.modules.concepts.collections.domain.exceptions;

public class InvalidCreateCollectionCommandException extends Throwable {
    public InvalidCreateCollectionCommandException(String message) {
        super(message);
    }
}
