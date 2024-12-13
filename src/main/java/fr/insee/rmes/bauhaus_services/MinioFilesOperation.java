package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesFileException;
import io.minio.*;
import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.util.Objects.requireNonNull;

public record MinioFilesOperation(MinioClient minioClient, String bucketName, String directoryGestion, String directoryPublication) implements FilesOperations {

    @Override
    public byte[] read(String pathFile){
        String objectName = getObjectName(requireNonNull(pathFile));
        try(InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build())) {
            return inputStream.readAllBytes();
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException(pathFile, "Error reading file: " + pathFile+" as object `"+objectName+"` in bucket "+bucketName, e);
        }
    }

    private String getObjectName(String pathFile) {
        return getObjectName(Path.of(pathFile));
    }

    private String getObjectName(Path pathFile) {
        return directoryGestion + "/" + pathFile.getFileName().toString();
    }


    @Override
    public void write(InputStream content, Path filePath) {
        String objectName = getObjectName(requireNonNull(filePath));
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(content, content.available(), -1)
                    .build());
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RmesFileException(filePath.toString(), "Error writing file: " + filePath+ "as object `"+objectName+"` in bucket "+bucketName, e);
        }
    }

    @Override
    public void copy(String srcObjectName, String destObjectName)  {

        String srcObject = getObjectName(requireNonNull(srcObjectName));
        String destObject = getObjectName(requireNonNull(srcObjectName));
        try {
            CopySource source = CopySource.builder()
                    .bucket(bucketName)
                    .object(srcObject)
                    .build();
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(destObject)
                    .source(source)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException(srcObjectName,"Error copying file from `" + srcObject + "` to `" + destObject+"` in bucket "+bucketName, e);
        }
    }


    @Override
    public boolean dirExists(Path gestionStorageFolder) {
        return true;
    }

    @Override
    public boolean exists(Path path) {
        var objectName = getObjectName(requireNonNull(path));
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()).size() > 0;
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            return false;
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException(objectName,"Error deleting file: " + objectName+" in bucket "+bucketName, e);
        }
    }

}

