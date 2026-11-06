package fr.insee.rmes.modules.operations.documents.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import java.io.InputStream;

/** Stockage des fichiers des documents en gestion. */
@ServerSidePort
public interface DocumentFileStorage {

    boolean exists(String fileName);

    /** @return l'URL sous laquelle le document désigne désormais ce fichier */
    String write(String fileName, InputStream content);

    InputStream read(String fileName);

    void delete(String fileName);

    /** Nom du fichier désigné par l'URL d'un document. */
    String fileNameOf(String url);
}
