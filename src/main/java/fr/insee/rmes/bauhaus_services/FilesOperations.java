package fr.insee.rmes.bauhaus_services;

import java.io.InputStream;
import java.nio.file.Path;

public interface FilesOperations {
    void delete(Path absolutePath);
    InputStream read(String filename);
    void write(InputStream content, Path destPath);
    void copy(String srcPath, String destPath);

    boolean dirExists(Path gestionStorageFolder);

    boolean existsInStorage(String filename);
}
