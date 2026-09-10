package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * One result of {@code POST _query/advanced} (when {@code resultsIncludeAll} is set). Unlike
 * {@link ColecticaItem} — which carries flat {@code ItemName}/{@code Label}/{@code VersionDate}
 * fields from the plain {@code _query} — the advanced endpoint groups metadata into typed property
 * bags keyed by property name:
 * <ul>
 *   <li>{@code TextProperties}: each key (e.g. {@code dcTitle}, {@code label}) maps to a list of
 *       localized values;</li>
 *   <li>{@code DateProperties}: each key (e.g. {@code versionDate}) maps to a list of ISO date
 *       strings;</li>
 *   <li>{@code BooleanProperties}: each key (e.g. {@code isPublished}) maps to a boolean.</li>
 * </ul>
 */
public record ColecticaAdvancedItem(
        @JsonProperty("AgencyId") String agencyId,
        @JsonProperty("Identifier") String identifier,
        @JsonProperty("Version") Integer version,
        @JsonProperty("ItemType") String itemType,
        @JsonProperty("IsDeprecated") Boolean isDeprecated,
        @JsonProperty("TextProperties") Map<String, List<LocalizedText>> textProperties,
        @JsonProperty("DateProperties") Map<String, List<String>> dateProperties,
        @JsonProperty("BooleanProperties") Map<String, Boolean> booleanProperties) {}
