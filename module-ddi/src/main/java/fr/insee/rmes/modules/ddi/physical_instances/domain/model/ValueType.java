package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DDI 4 {@code ValueType} container. Used wherever the schema declares
 * {@code Value: ValueType} (e.g. {@code CodeType.Value}). Carries a single
 * {@code StringValue}; further fields can be added if the schema grows.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValueType(
        @JsonProperty("StringValue") String stringValue
) {

    public static ValueType of(String stringValue) {
        return stringValue == null ? null : new ValueType(stringValue);
    }
}
