package fr.insee.rmes.modules.operations.documents.domain.model;

import java.io.InputStream;

/** Fichier déposé par l'utilisateur : son nom d'origine, son contenu, et sa taille en octets. */
public record UploadedFile(String name, InputStream content, long size) {}
