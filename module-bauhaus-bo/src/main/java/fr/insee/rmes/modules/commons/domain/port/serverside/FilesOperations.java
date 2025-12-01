package fr.insee.rmes.modules.commons.domain.port.serverside;

import fr.insee.rmes.modules.commons.domain.model.Document;

import java.io.InputStream;

public interface FilesOperations {
    default void delete(Document document){
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    InputStream read(Document document);

    void write(InputStream content, Document targetDocument);

    void copy(Document srcPath, Document targetPath);

    boolean exists(String path);

    boolean exists(Document document);
}
