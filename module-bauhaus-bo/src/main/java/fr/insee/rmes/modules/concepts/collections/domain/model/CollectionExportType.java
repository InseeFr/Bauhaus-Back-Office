package fr.insee.rmes.modules.concepts.collections.domain.model;

public enum CollectionExportType {
    ODT, ODS;

    public static CollectionExportType fromString(String value) {
        return CollectionExportType.valueOf(value.toUpperCase());
    }
}
