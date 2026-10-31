package fr.insee.rmes.modules.commons.infrastructure.minio;

import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerSideAdaptor
public record MinioFilesOperation(MinioClient minioClient, String bucketName) implements FilesOperations {

    /**
     *
     */
    static final Logger logger = LoggerFactory.getLogger(MinioFilesOperation.class);

    private static final String NO_SUCH_KEY = "NoSuchKey";

    @Override
    public InputStream read(Document document) {
        // String objectName = directoryGestion + "/" + filename;

        logger.debug("Reading file from path {} in bucket {}", document.getFullPath(), bucketName);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(document.getFullPath())
                    .build());
        } catch (MinioException e) {
            throw new RmesFileException(
                    document.name(), "Error reading file: " + document.getFullPath() + "  in bucket " + bucketName, e);
        }
    }

    @Override
    public void write(InputStream content, Document targetDocument) {
        logger.debug(
                "Writing to file with name {} from path {} in bucket {}",
                targetDocument.name(),
                targetDocument.getFullPath(),
                bucketName);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(targetDocument.getFullPath()).stream(
                                    content, (long) content.available(), -1L)
                            .build());
        } catch (MinioException | IOException e) {
            throw new RmesFileException(
                    targetDocument.name(),
                    "Error writing file: " + targetDocument.name() + " in bucket " + bucketName,
                    e);
        }
    }

    @Override
    public void copy(Document srcDocument, Document targetDocument) {
        String srcFullPath = srcDocument.getFullPath();
        String targetFullPath = targetDocument.getFullPath();

        logger.debug(
                "Copy from source {} as object {} to destination {} in bucket {}",
                srcFullPath,
                targetFullPath,
                bucketName);

        try {
            SourceObject source = SourceObject.builder()
                    .bucket(bucketName)
                    .object(srcFullPath)
                    .build();

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(targetFullPath)
                    .source(source)
                    .build());
        } catch (MinioException e) {
            throw new RmesFileException(
                    srcFullPath,
                    "Error copying file from `" + srcFullPath + "` to `" + targetFullPath + "` in bucket " + bucketName,
                    e);
        }
    }

    @Override
    public boolean exists(Document document) {
        String objectName = document.getFullPath();
        logger.debug("Check existence of file with name {} in bucket {}", objectName, bucketName);
        try {
            return minioClient
                            .statObject(StatObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .build())
                            .size()
                    > 0;
        } catch (ErrorResponseException e) {
            if (NO_SUCH_KEY.equals(e.errorResponse().code())) {
                return false;
            }
            throw existenceCheckFailure(objectName, e);
        } catch (MinioException | IllegalStateException e) {
            throw existenceCheckFailure(objectName, e);
        }
    }

    /** Un « répertoire » MinIO n'est qu'un préfixe : il existe dès qu'un objet vit dessous. */
    @Override
    public boolean exists(String path) {
        String prefix = path.endsWith("/") ? path : path + "/";
        logger.debug("Check existence of directory {} in bucket {}", prefix, bucketName);
        try {
            Iterator<Result<Item>> objects = minioClient
                    .listObjects(ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .maxKeys(1)
                            .build())
                    .iterator();
            if (!objects.hasNext()) {
                return false;
            }
            // Result.get() relève l'erreur de la requête de listage (MinIO injoignable, bucket absent…)
            objects.next().get();
            return true;
        } catch (MinioException | IllegalStateException e) {
            throw existenceCheckFailure(prefix, e);
        }
    }

    /**
     * MinioClient relance les {@link MinioException} et enveloppe toute autre cause (connexion
     * refusée, timeout…) dans une {@link IllegalStateException} : les deux signalent un MinIO
     * incapable de répondre, pas un fichier absent.
     */
    private RmesFileException existenceCheckFailure(String objectName, Exception cause) {
        return new RmesFileException(
                objectName, "Error checking existence of: " + objectName + " in bucket " + bucketName, cause);
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
        } catch (MinioException e) {
            throw new RmesFileException(
                    objectName, "Error deleting file: " + objectName + " in bucket " + bucketName, e);
        }
    }
}
