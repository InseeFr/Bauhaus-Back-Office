package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorIdException;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IndicatorId other)) {
            return false;
        }
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
