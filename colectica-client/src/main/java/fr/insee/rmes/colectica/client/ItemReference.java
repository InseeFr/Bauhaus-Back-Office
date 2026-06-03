package fr.insee.rmes.colectica.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lightweight reference to a Colectica item (agency + identifier), as returned by the
 * {@code _query/relationship/.../descriptions} endpoints. Carries no version nor item content.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemReference(
    @JsonProperty("AgencyId") String agencyId,
    @JsonProperty("Identifier") String identifier
) {}
