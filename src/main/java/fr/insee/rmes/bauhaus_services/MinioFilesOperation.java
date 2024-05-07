package fr.insee.rmes.bauhaus_services;

import io.minio.*;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public record MinioFilesOperation(MinioClient minioClient, String bucketName) implements FilesOperations {

    private static final Logger logger = LoggerFactory.getLogger(MinioFilesOperation.class);

    @Override
    public InputStream read(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            logger.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Error reading file: " + objectName, e);
        }
    }

    @Override
    public void write(InputStream content, Path objectName) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName.getFileName().toString())
                    .stream(content, content.available(), -1)
                    .build());
        } catch (Exception e) {
            logger.error("Error writing file: {}", e.getMessage());
            throw new RuntimeException("Error writing file: " + objectName, e);
        }
    }

    @Override
    public void copy(String srcObjectName, String destObjectName) {
        try {
            CopySource source = CopySource.builder()
                    .bucket(bucketName)
                    .object(srcObjectName)
                    .build();

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(destObjectName)
                    .source(source)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            logger.error("Error copying file from {} to {}: {}", srcObjectName, destObjectName, e.getMessage());
            throw new RuntimeException("Error copying file from " + srcObjectName + " to " + destObjectName, e);
        }
    }


    @Override
    public boolean dirExists(Path gestionStorageFolder) {
        return true;
    }

    public void uploadFile(String bucketName, String objectName, InputStream documentFile, long size, String contentType) throws Exception {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(documentFile, size, -1)
                            .contentType(contentType)
                            .build());
        } catch (MinioException e) {
            throw new Exception("Erreur lors de l'upload du fichier : " + e.getMessage(), e);
        }
    }
    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (MinioException e) {
            logger.error("Erreur lors de la suppression du fichier : {}", e.getMessage());
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
            logger.error("Error deleting file: {}", e.getMessage());
            throw new RuntimeException("Error deleting file: " + objectName, e);
        }
    }

    public InputStream downloadFile(String bucketName, String objectName) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (MinioException e) {
            throw new Exception("Erreur lors du téléchargement du fichier : " + e.getMessage(), e);
        }
    }
}

