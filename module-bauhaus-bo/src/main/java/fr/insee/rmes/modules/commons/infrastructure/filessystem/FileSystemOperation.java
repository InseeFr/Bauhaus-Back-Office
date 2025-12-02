package fr.insee.rmes.modules.commons.infrastructure.filessystem;

import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;

@ServerSideAdaptor
public class FileSystemOperation implements FilesOperations {

    @Override
    public void delete(Document document) {
        try {
            Files.delete(Path.of(document.getFullPath()));
        } catch (IOException e) {
            throw new RmesFileException(document.getFullPath(), "Failed to delete file", e);
        }
    }

    @Override
    public InputStream read(Document document) {
        try {
            return Files.newInputStream(Path.of(document.getFullPath()));
        } catch (IOException e) {
            throw new RmesFileException(document.name(), "Failed to read file: " + document.getFullPath(), e);
        }
    }

    @Override
    public boolean exists(Document document) {
        return Files.exists(Path.of(document.getFullPath()));
    }

    @Override
    public void write(InputStream content, Document document) {
        try {
            Files.copy(content, Path.of(document.getFullPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RmesFileException(document.name(),"Failed to write file: " + document.getFullPath(), e);
        }
    }

    @Override
    public void copy(Document srcDocument, Document targetDocument)  {
        Path file = Paths.get(srcDocument.getFullPath());
        Path targetPath = Paths.get(targetDocument.getFullPath());
        try {
            Files.copy(file, targetPath.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RmesFileException(srcDocument.getFullPath(), "Failed to copy file : " + srcDocument.getFullPath() + " to " + targetDocument.getFullPath(), e);
        }
    }

    @Override
    public boolean exists(String gestionStorageFolder) {
        return Files.isDirectory(Path.of(requireNonNull(gestionStorageFolder)));
    }

}
