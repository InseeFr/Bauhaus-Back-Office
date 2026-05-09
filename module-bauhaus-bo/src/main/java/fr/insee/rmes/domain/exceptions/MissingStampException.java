package fr.insee.rmes.domain.exceptions;

public class MissingStampException extends RuntimeException {
    public MissingStampException(String message) {
        super(message);
    }
}
