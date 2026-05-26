package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DDI 4 {@code cogsDate} type. Carries one of {@code DateTime}, {@code Date},
 * {@code GYearMonth}, {@code GYear}, {@code Duration}. The most common case
 * (a full date-time) is exposed via {@link #ofDateTime}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CogsDate(
        @JsonProperty("DateTime") String dateTime,
        @JsonProperty("Date") String date,
        @JsonProperty("GYearMonth") String gYearMonth,
        @JsonProperty("GYear") String gYear,
        @JsonProperty("Duration") Number duration
) {

    public static CogsDate ofDateTime(String dateTime) {
        return new CogsDate(dateTime, null, null, null, null);
    }
}
