package fr.insee.rmes.modules.operations.documents.infrastructure.storage;

import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentFileStorage;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Fichiers des documents en gestion, sur le stockage configuré (disque ou minio). Tout se lit et
 * s'écrit sous un même répertoire ; l'URL enregistrée sur le document garde sa forme historique,
 * {@code file://<répertoire de gestion>/<nom>}, dont seul le nom sert à retrouver le fichier.
 */
@ServerSideAdaptor
public class FilesOperationsDocumentFileStorage implements DocumentFileStorage {

    private static final String SCHEME_FILE = "file://";

    private final FilesOperations filesOperations;
    private final String directory;
    private final String urlBase;

    /**
     * @param directory répertoire (ou préfixe d'objet minio) où vivent les fichiers
     * @param urlBase chemin placé dans l'URL enregistrée sur le document
     */
    public FilesOperationsDocumentFileStorage(FilesOperations filesOperations, String directory, String urlBase) {
        this.filesOperations = filesOperations;
        this.directory = directory;
        this.urlBase = urlBase;
    }

    @Override
    public boolean exists(String fileName) {
        return filesOperations.exists(file(fileName));
    }

    @Override
    public String write(String fileName, InputStream content) {
        filesOperations.write(content, file(fileName));
        String url = Path.of(urlBase).resolve(fileName).toString();
        return url.matches("^[a-zA-Z]+:/.*") ? url : SCHEME_FILE + url;
    }

    @Override
    public InputStream read(String fileName) {
        return filesOperations.read(file(fileName));
    }

    @Override
    public void delete(String fileName) {
        filesOperations.delete(file(fileName));
    }

    @Override
    public String fileNameOf(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private Document file(String fileName) {
        return new Document(directory, fileName);
    }
}
