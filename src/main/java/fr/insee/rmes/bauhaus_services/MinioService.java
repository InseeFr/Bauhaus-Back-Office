package fr.insee.rmes.bauhaus_services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;
    static final Logger logger = LoggerFactory.getLogger(MinioService.class);
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

