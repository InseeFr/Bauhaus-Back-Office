package fr.insee.rmes.exceptions;

public class RmesRuntimeBadRequestException extends RuntimeException {
    public RmesRuntimeBadRequestException(String message) {
        super((message));
    }
}
