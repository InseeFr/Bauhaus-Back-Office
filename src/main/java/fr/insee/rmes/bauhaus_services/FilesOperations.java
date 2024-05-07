package fr.insee.rmes.bauhaus_services;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

public interface FilesOperations {
    void delete(String path);
    InputStream read(String path);
    void write(InputStream content, Path destPath);
    void copy(String srcPath, String destPath);

    boolean dirExists(Path gestionStorageFolder);
}
