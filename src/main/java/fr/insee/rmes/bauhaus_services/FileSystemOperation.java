package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesFileException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import static java.util.Objects.requireNonNull;

public class FileSystemOperation implements FilesOperations {

    @Autowired
    protected Config config;

    @Override
    public void delete(String path) {
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            throw new RmesFileException(path, "Failed to delete file: " + path, e);
        }
    }

    @Override
    public byte[] read(String fileName) {
        try {
            return Files.readAllBytes(Paths.get(config.getDocumentsStorageGestion()).resolve(fileName));
        } catch (IOException e) {
            throw new RmesFileException(fileName, "Failed to read file: " + fileName, e);
        }
    }

    @Override
    public void write(InputStream content, Path destPath) {
        try {
            Files.copy(content, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RmesFileException(destPath.toString(),"Failed to write file: " + destPath, e);
        }
    }

    @Override
    public void copy(String srcPath, String destPath)  {
        Path file = Paths.get(srcPath);
        Path targetPath = Paths.get(destPath);
        try {
            Files.copy(file, targetPath.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RmesFileException(srcPath, "Failed to copy file : " + srcPath + " to " + destPath, e);
        }
    }

    @Override
    public boolean dirExists(Path gestionStorageFolder) {
        return Files.isDirectory(requireNonNull(gestionStorageFolder));
    }

    @Override
    public boolean exists(Path path) {
        return false;
    }
}
