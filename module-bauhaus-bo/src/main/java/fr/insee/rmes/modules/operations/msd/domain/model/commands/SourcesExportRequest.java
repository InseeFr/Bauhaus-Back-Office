package fr.insee.rmes.modules.operations.msd.domain.model.commands;

public record SourcesExportRequest(String id, boolean includeEmptyMas, boolean lg1, boolean lg2) {}
