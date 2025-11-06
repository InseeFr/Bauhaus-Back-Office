package fr.insee.rmes.modules.operations.msd.domain;

public class NotFoundAttributeException extends Exception {
    private final String id;

    public NotFoundAttributeException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
