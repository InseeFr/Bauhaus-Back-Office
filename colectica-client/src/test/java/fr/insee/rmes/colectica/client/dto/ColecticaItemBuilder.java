package fr.insee.rmes.colectica.client.dto;

import java.util.List;
import java.util.Map;

/**
 * Test data builder for {@link ColecticaItem}: only the type and identifier are mandatory, every other
 * field gets a realistic default (published DDI item of agency {@code fr.insee}, version 1).
 *
 * <p>Shared with the other modules through the {@code colectica-client} test-jar.
 */
public final class ColecticaItemBuilder {

    private final String itemType;
    private final String identifier;
    private String agencyId = "fr.insee";
    private Integer version = 1;
    private Map<String, String> itemName;
    private Map<String, String> label;
    private String versionDate;
    private String versionResponsibility;
    private Boolean isPublished = true;
    private String itemFormat = "DDI";

    private ColecticaItemBuilder(String itemType, String identifier) {
        this.itemType = itemType;
        this.identifier = identifier;
    }

    public static ColecticaItemBuilder aColecticaItem(String itemType, String identifier) {
        return new ColecticaItemBuilder(itemType, identifier);
    }

    public ColecticaItemBuilder agency(String agencyId) {
        this.agencyId = agencyId;
        return this;
    }

    public ColecticaItemBuilder version(int version) {
        this.version = version;
        return this;
    }

    /** French item name; {@code null} leaves the item without any name. */
    public ColecticaItemBuilder itemName(String frenchName) {
        this.itemName = frenchName == null ? null : Map.of("fr-FR", frenchName);
        return this;
    }

    public ColecticaItemBuilder itemName(Map<String, String> itemName) {
        this.itemName = itemName;
        return this;
    }

    /** French label; {@code null} leaves the item without any label. */
    public ColecticaItemBuilder label(String frenchLabel) {
        this.label = frenchLabel == null ? null : Map.of("fr-FR", frenchLabel);
        return this;
    }

    public ColecticaItemBuilder label(Map<String, String> label) {
        this.label = label;
        return this;
    }

    /** Same French text as item name and label, as Colectica returns for most items. */
    public ColecticaItemBuilder named(String frenchName) {
        return itemName(frenchName).label(frenchName);
    }

    public ColecticaItemBuilder versionDate(String versionDate) {
        this.versionDate = versionDate;
        return this;
    }

    public ColecticaItemBuilder versionResponsibility(String versionResponsibility) {
        this.versionResponsibility = versionResponsibility;
        return this;
    }

    public ColecticaItemBuilder published(boolean isPublished) {
        this.isPublished = isPublished;
        return this;
    }

    public ColecticaItemBuilder itemFormat(String itemFormat) {
        this.itemFormat = itemFormat;
        return this;
    }

    public ColecticaItem build() {
        return new ColecticaItem(
                null, // summary
                itemName,
                label,
                null, // description
                null, // versionRationale
                0, // metadataRank
                "test-repo", // repositoryName
                true, // isAuthoritative
                List.of(), // tags
                itemType,
                agencyId,
                version,
                identifier,
                null, // item
                null, // notes
                versionDate,
                versionResponsibility,
                isPublished,
                false, // isDeprecated
                false, // isProvisional
                itemFormat,
                1L, // transactionId
                0); // versionCreationType
    }
}
