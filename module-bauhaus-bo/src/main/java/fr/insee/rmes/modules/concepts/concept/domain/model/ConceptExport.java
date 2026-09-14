package fr.insee.rmes.modules.concepts.concept.domain.model;

public record ConceptExport(String fileName, byte[] content, String contentType) {}
