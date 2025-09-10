package fr.insee.rmes.bauhaus_services;

import java.io.InputStream;
import java.nio.file.Path;

public interface FilesOperations {
    default void delete(Path absolutePath){
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    InputStream readInDirectoryGestion(String filename);
    void writeToDirectoryGestion(InputStream content, Path destPath);
    void copyFromGestionToPublication(String srcPath, String destPath);

    boolean dirExists(Path gestionStorageFolder);

    boolean existsInStorageGestion(String filename);
}
