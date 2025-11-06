package fr.insee.rmes.modules.commons.domain;

public class GenericInternalServerException extends Exception {

    private final String details;

    public GenericInternalServerException(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
