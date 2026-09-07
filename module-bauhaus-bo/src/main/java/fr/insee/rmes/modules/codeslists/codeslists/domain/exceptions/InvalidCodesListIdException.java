package fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions;

public class InvalidCodesListIdException extends RuntimeException {
    public InvalidCodesListIdException(String message) {
        super(message);
    }
}
