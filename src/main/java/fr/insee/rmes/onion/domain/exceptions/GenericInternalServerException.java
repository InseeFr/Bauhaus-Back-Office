package fr.insee.rmes.onion.domain.exceptions;

public class GenericInternalServerException extends Exception {

    private final String details;

    public GenericInternalServerException(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}
