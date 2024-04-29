package fr.insee.rmes.bauhaus_services;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
@Service
public class FileSystemOperation implements FilesOperations {
    @Override
    public void delete(String path) {
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    @Override
    public InputStream read(String fileName) {
        try {
            return Files.newInputStream(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + fileName, e);
        }
    }

    @Override
    public void write(InputStream content, String destPath) {
        try {
            Files.copy(content, Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + destPath, e);
        }
    }

    @Override
    public void copy(String srcPath, String destPath) {
        try {
            Files.copy(Paths.get(srcPath), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file from " + srcPath + " to " + destPath, e);
        }
    }
}
