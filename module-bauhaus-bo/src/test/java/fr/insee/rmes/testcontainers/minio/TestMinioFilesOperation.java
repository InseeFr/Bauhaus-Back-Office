package fr.insee.rmes.testcontainers.minio;

import fr.insee.rmes.bauhaus_services.MinioFilesOperation;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Testcontainers
class TestMinioFilesOperation {

    @Container
    MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2024-11-07T00-52-20Z");

    @BeforeAll
    public static void configureSlf4j() {
        System.setProperty("org.slf4j.simpleLogger.log."+MinioFilesOperation.class.getName(), "debug");
        System.setProperty("slf4j.provider", "org.slf4j.simple.SimpleServiceProvider");
    }

    @Test
    void testWritingThenCheckExistThenCopyThenRead_shouldBeOK() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var nomFichier = "test.txt";
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(container.getS3URL())
                .credentials(container.getUserName(), container.getPassword())
                .build();
        MinioFilesOperation minioFilesOperation = new MinioFilesOperation(minioClient,"metadata", "gestion", "publication");
        createBucket(minioFilesOperation.bucketName(), minioClient);

        Path absolutePathInGestion = Path.of("/mnt/applishare/rmes/data/storage/documents").resolve(nomFichier);
        String contenuFichier = "Test";
        minioFilesOperation.writeToDirectoryGestion(new ByteArrayInputStream(contenuFichier.getBytes()), absolutePathInGestion);
        assertThat(minioFilesOperation.dirExists(Path.of(minioFilesOperation.directoryGestion()))).isTrue();
        assertThat(minioFilesOperation.existsInStorageGestion(nomFichier)).isTrue();

        minioFilesOperation.copyFromGestionToPublication(String.valueOf(absolutePathInGestion), "/mnt/applishare/rmes/data/storage/documents/tempPub1");
        assertThat(minioFilesOperation.dirExists(Path.of(minioFilesOperation.directoryPublication()))).isTrue();
        assertThat(fileExistsInPublication(minioClient, minioFilesOperation, nomFichier)
        ).isTrue();

        assertThat(new String(minioFilesOperation.readInDirectoryGestion(nomFichier).readAllBytes())).isEqualTo(contenuFichier);
    }


    private static boolean fileExistsInPublication(MinioClient minioClient, MinioFilesOperation minioFilesOperation, String nomFichier) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(minioFilesOperation.bucketName())
                        .object(minioFilesOperation.directoryPublication() + "/" + nomFichier).build()
        ).size() > 0;
    }

    private void createBucket(String bucketName, MinioClient minioClient) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

}
