package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single localized value as returned inside the {@code TextProperties} bag of the
 * {@code _query/advanced} response, e.g. {@code {"Value": "Ma Physical Instance", "LanguageTag": "fr-FR"}}.
 */
public record LocalizedText(
        @JsonProperty("Value") String value,
        @JsonProperty("LanguageTag") String languageTag) {}
