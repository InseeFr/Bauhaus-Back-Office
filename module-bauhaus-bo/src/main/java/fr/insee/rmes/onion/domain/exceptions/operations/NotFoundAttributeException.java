package fr.insee.rmes.onion.domain.exceptions.operations;

public class NotFoundAttributeException extends Exception {
    private final String id;

    public NotFoundAttributeException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
