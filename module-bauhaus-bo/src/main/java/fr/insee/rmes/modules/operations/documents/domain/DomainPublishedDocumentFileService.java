package fr.insee.rmes.modules.operations.documents.domain;

import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.PublishedFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentFileFormat;
import fr.insee.rmes.modules.operations.documents.domain.model.PublishedFile;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.PublishedDocumentFileService;

/**
 * Sert les fichiers diffusés : ceux que la publication d'un rapport qualité a copiés dans le
 * répertoire de publication. Les fichiers en cours de gestion ne sont jamais servis.
 */
public class DomainPublishedDocumentFileService implements PublishedDocumentFileService {

    private final FilesOperations filesOperations;
    private final String publicationDirectory;

    public DomainPublishedDocumentFileService(FilesOperations filesOperations, String publicationDirectory) {
        this.filesOperations = filesOperations;
        this.publicationDirectory = publicationDirectory;
    }

    @Override
    public PublishedFile getPublishedFile(String fileName) throws PublishedFileNotFoundException {
        Document file = new Document(publicationDirectory, fileName);
        if (!isPlainFileName(fileName) || !filesOperations.exists(file)) {
            throw new PublishedFileNotFoundException(fileName);
        }
        return new PublishedFile(fileName, DocumentFileFormat.mediaTypeOf(fileName), filesOperations.read(file));
    }

    /** Un simple nom de fichier : rien qui permette de sortir du répertoire de publication. */
    private static boolean isPlainFileName(String fileName) {
        return !fileName.isBlank()
                && !fileName.equals(".")
                && !fileName.equals("..")
                && fileName.chars().noneMatch(c -> c == '/' || c == '\\' || Character.isISOControl(c));
    }
}
