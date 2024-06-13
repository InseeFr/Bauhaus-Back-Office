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

public record MinioFilesOperation(MinioClient minioClient, String bucketName, String directoryGestion, String directoryPublication) implements FilesOperations {

    private static final Logger logger = LoggerFactory.getLogger(MinioFilesOperation.class);

    @Override
    public InputStream read(String pathFile) {
        String objectName= extractFileName(pathFile);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(directoryGestion +"/"+ objectName)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            logger.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Error reading file: " + objectName, e);
        }
    }
    public static String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return filePath; // The path does not contain a slash, return the whole string
        }
        return filePath.substring(lastSlashIndex + 1);
    }


    @Override
    public void write(InputStream content, Path objectName) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(directoryGestion +"/"+ objectName.getFileName().toString())
                    .stream(content, content.available(), -1)
                    .build());
        } catch (Exception e) {
            logger.error("Error writing file: {}", e.getMessage());
            throw new RuntimeException("Error writing file: " + objectName, e);
        }
    }

    @Override
    public void copy(String srcObjectName, String destObjectName)  {

        try {
            CopySource source = CopySource.builder()
                    .bucket(bucketName)
                    .object(directoryGestion +"/"+ extractFileName(srcObjectName))
                    .build();

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(directoryPublication +"/"+ extractFileName(srcObjectName))
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

}

