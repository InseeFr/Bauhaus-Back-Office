package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesFileException;
import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public record MinioFilesOperation(MinioClient minioClient, String bucketName, String directoryGestion, String directoryPublication) implements FilesOperations {

    private static final Logger logger = LoggerFactory.getLogger(MinioFilesOperation.class);

    @Override
    public InputStream read(String pathFile){
        String objectName= extractFileName(pathFile);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(directoryGestion +"/"+ objectName)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException("Error reading file: " + objectName, e);
        }
    }
    private static String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return Path.of(filePath).getFileName().toString();
    }


    @Override
    public void write(InputStream content, Path objectName) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(directoryGestion +"/"+ objectName.getFileName().toString())
                    .stream(content, content.available(), -1)
                    .build());
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RmesFileException("Error writing file: " + objectName, e);
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
            throw new RmesFileException("Error copying file from " + srcObjectName + " to " + destObjectName, e);
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
            throw new RmesFileException("Error deleting file: " + objectName, e);
        }
    }

}

