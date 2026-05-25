package fr.insee.rmes.modules.operations.msd.domain.model.commands;

public record MetadataExportRequest(
        String id,
        boolean includeEmptyMas,
        boolean lg1,
        boolean lg2,
        boolean includeDocuments
) {
}
