package fr.insee.rmes.modules.concepts.collections.domain.model;

public record CollectionExport(String fileName, byte[] content, String contentType) {}
