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

    static final Logger logger = LoggerFactory.getLogger(MinioFilesOperation.class);

    @Override
    public InputStream readInDirectoryGestion(String filename){
        String objectName = directoryGestion + "/" + filename;

        logger.debug("Reading file with name {} from path {} as object {} in bucket {}", filename, filename, objectName, bucketName);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException(filename, "Error reading file: " + filename+" as object `"+objectName+"` in bucket "+bucketName, e);
        }
    }
    private static String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return Path.of(filePath).getFileName().toString();
    }

    @Override
    public boolean existsInStorageGestion(String filename) {
        String objectName = directoryGestion + "/" + filename;
        logger.debug("Check existence of file with name {} as object {} in bucket {}", filename, objectName, bucketName);
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
    public void writeToDirectoryGestion(InputStream content, Path filePath) {
        String filename = filePath.getFileName().toString();
        String objectName = directoryGestion + "/" + filename;
        logger.debug("Writing to file with name {} from path {} as object {} in bucket {}", filename, filePath, objectName, bucketName);
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
    public void copyFromGestionToPublication(String srcObjectName, String destObjectName)  {

        String srcObject = directoryGestion + "/" + extractFileName(srcObjectName);
        String destObject = directoryPublication + "/" + extractFileName(srcObjectName);

        logger.debug("Copy from source {} as object {} to destination {} as object {} in bucket {}", srcObjectName, srcObject, destObjectName, destObject, bucketName);

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
    public void delete(Path absolutePath) {
        String objectName = absolutePath.getFileName().toString();

        logger.debug("Delete file with path {} as object {} in bucket {}", absolutePath, objectName, bucketName);

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

