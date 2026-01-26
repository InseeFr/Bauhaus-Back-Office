package fr.insee.rmes.modules.concepts.collections.domain.exceptions;

public class CollectionsFetchException extends Throwable {
    public CollectionsFetchException(Exception e) {
        super(e);
    }
    public CollectionsFetchException(String message){
        super(message);
    }
}
