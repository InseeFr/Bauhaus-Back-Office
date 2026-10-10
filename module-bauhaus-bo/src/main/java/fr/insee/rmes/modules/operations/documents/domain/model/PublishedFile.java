package fr.insee.rmes.modules.operations.documents.domain.model;

import java.io.InputStream;

/** Fichier diffusé : son nom, le type MIME sous lequel le servir, et son contenu à lire une fois. */
public record PublishedFile(String name, String mediaType, InputStream content) {}
