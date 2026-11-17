package fr.insee.rmes.modules.operations.documents.domain.model;

import java.io.InputStream;

/** Fichier d'un document en gestion, à lire une fois. */
public record StoredFile(String name, InputStream content) {}
