package fr.insee.rmes.modules.commons.infrastructure.minio;

import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@ServerSideAdaptor
public record MinioFilesOperation(MinioClient minioClient, String bucketName) implements FilesOperations {

    /**
     *
     */
    static final Logger logger = LoggerFactory.getLogger(MinioFilesOperation.class);

    @Override
    public InputStream read(Document document){
        //String objectName = directoryGestion + "/" + filename;

        logger.debug("Reading file from path {} in bucket {}", document.getFullPath(), bucketName);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(document.getFullPath())
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException(document.name(), "Error reading file: " + document.getFullPath()+"  in bucket "+bucketName, e);
        }
    }

    @Override
    public void write(InputStream content, Document targetDocument) {
        logger.debug("Writing to file with name {} from path {} in bucket {}", targetDocument.name(), targetDocument.getFullPath(), bucketName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(targetDocument.getFullPath())
                    .stream(content, content.available(), -1)
                    .build());
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RmesFileException(targetDocument.name(), "Error writing file: " + targetDocument.name()+ " in bucket "+bucketName, e);
        }
    }

    @Override
    public void copy(Document srcDocument, Document targetDocument)  {
        String srcFullPath = srcDocument.getFullPath();
        String targetFullPath = targetDocument.getFullPath();


        logger.debug("Copy from source {} as object {} to destination {} in bucket {}", srcFullPath, targetFullPath, bucketName);

        try {
            CopySource source = CopySource.builder()
                    .bucket(bucketName)
                    .object(srcFullPath)
                    .build();

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(targetFullPath)
                    .source(source)
                    .build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new RmesFileException(srcFullPath,"Error copying file from `" + srcFullPath + "` to `" + targetFullPath+"` in bucket "+bucketName, e);
        }
    }


    @Override
    public boolean exists(Document document) {
        //String objectName = directoryGestion + "/" + filename;
        logger.debug("Check existence of file with name {} in bucket {}", document.getFullPath(), bucketName);
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(document.getFullPath())
                    .build()).size() > 0;
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            return false;
        }
    }


    @Override
    public boolean exists(String path) {
        return true;
    }

     @Override
    public void delete(Document document) {
        String objectName = document.getFullPath();

        logger.debug("Delete file with path {} in bucket {}", objectName, bucketName);

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

