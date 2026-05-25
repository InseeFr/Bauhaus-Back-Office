package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

/**
 * Represents a DDI 3.3 response format
 */
public record Ddi3Response(
    Ddi3Options options,
    List<Ddi3Item> items
) {
    public record Ddi3Options(
        List<String> namedOptions
    ) {}

    public record Ddi3Item(
        String itemType,
        String agencyId,
        String version,
        String identifier,
        String item,
        String versionDate,
        String versionResponsibility,
        boolean isPublished,
        boolean isDeprecated,
        boolean isProvisional,
        String itemFormat
    ) {}
}
