package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorIdException;

public class IndicatorId {
    private final String value;

    public IndicatorId(String value) throws InvalidIndicatorIdException {
        if (value == null) {
            throw new InvalidIndicatorIdException("The identifier is null");
        }
        if (value.isEmpty()) {
            throw new InvalidIndicatorIdException("The identifier is empty");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
