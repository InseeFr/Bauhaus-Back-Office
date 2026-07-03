package fr.insee.rmes.modules.operations.indicators.domain.exceptions;

public class InvalidIndicatorIdException extends RuntimeException {
    public InvalidIndicatorIdException(String message) {
        super(message);
    }
}
