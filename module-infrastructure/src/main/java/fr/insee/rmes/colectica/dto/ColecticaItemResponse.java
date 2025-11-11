package fr.insee.rmes.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ColecticaItemResponse(
    @JsonProperty("ItemType") String itemType,
    @JsonProperty("AgencyId") String agencyId,
    @JsonProperty("Version") int version,
    @JsonProperty("Identifier") String identifier,
    @JsonProperty("Item") String item,  // XML content as string
    @JsonProperty("VersionDate") String versionDate,
    @JsonProperty("VersionResponsibility") String versionResponsibility,
    @JsonProperty("IsPublished") boolean isPublished,
    @JsonProperty("IsDeprecated") boolean isDeprecated,
    @JsonProperty("IsProvisional") boolean isProvisional,
    @JsonProperty("ItemFormat") String itemFormat
) {}
