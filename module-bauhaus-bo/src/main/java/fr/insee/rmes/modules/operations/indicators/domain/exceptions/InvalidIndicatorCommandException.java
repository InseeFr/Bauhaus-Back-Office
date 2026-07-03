package fr.insee.rmes.modules.operations.indicators.domain.exceptions;

public class InvalidIndicatorCommandException extends Throwable {
    public InvalidIndicatorCommandException(String message) {
        super(message);
    }
}
