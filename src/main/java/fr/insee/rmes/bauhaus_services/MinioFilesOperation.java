package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesFileException;
import io.minio.*;
import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public record MinioFilesOperation(MinioClient minioClient, String bucketName, String directoryGestion, String directoryPublication) implements FilesOperations {

    @Override
    public InputStream read(String pathFile){
        String fileName= extractFileName(pathFile);
        String objectName = directoryGestion + "/" + fileName;
        try {

            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException(fileName, "Error reading file: " + fileName+" as object `"+objectName+"` in bucket "+bucketName, e);
        }
    }
    private static String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return Path.of(filePath).getFileName().toString();
    }


    @Override
    public void write(InputStream content, Path filePath) {
        String filename = filePath.getFileName().toString();
        String objectName = directoryGestion + "/" + filename;
        try {

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(content, content.available(), -1)
                    .build());
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RmesFileException(filePath.toString(), "Error writing file: " + filename+ "as object `"+objectName+"` in bucket "+bucketName, e);
        }
    }

    @Override
    public void copy(String srcObjectName, String destObjectName)  {

        String srcObject = directoryGestion + "/" + extractFileName(srcObjectName);
        String destObject = directoryPublication + "/" + extractFileName(srcObjectName);
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

