package fr.insee.rmes.bauhaus_services;

import java.io.InputStream;

public interface FilesOperations {
    void delete(String path);
    InputStream read(String path);
    void write(InputStream content, String destPath);
    void copy(String srcPath, String destPath);
}
