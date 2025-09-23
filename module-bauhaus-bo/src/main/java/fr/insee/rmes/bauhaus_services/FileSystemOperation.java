package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesFileException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;

public class FileSystemOperation implements FilesOperations {

    private final Config config;

    public FileSystemOperation(Config config) {
        this.config = config;
    }

    @Override
    public void delete(Path absolutePath) {
        try {
            Files.delete(absolutePath);
        } catch (IOException e) {
            throw new RmesFileException(absolutePath.getFileName().toString(), "Failed to delete file: " + absolutePath, e);
        }
    }

    @Override
    public InputStream readInDirectoryGestion(String fileName) {
        try {
            return Files.newInputStream(Paths.get(config.getDocumentsStorageGestion()).resolve(fileName));
        } catch (IOException e) {
            throw new RmesFileException(fileName, "Failed to read file: " + fileName, e);
        }
    }

    @Override
    public boolean existsInStorageGestion(String filename) {
        return Files.exists(Paths.get(config.getDocumentsStorageGestion()).resolve(filename));
    }

    @Override
    public void writeToDirectoryGestion(InputStream content, Path destPath) {
        try {
            Files.copy(content, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RmesFileException(destPath.toString(),"Failed to write file: " + destPath, e);
        }
    }

    @Override
    public void copyFromGestionToPublication(String srcPath, String destPath)  {
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

}
