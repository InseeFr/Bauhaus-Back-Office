package fr.insee.rmes.modules.operations.msd.domain.model;

public record SimsConvertedTextNode(String graph, String uri, String predicate, boolean needHTML, String markdown, String html, String lang) {}