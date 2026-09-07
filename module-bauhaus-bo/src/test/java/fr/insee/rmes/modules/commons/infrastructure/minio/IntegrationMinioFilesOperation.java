package fr.insee.rmes.modules.commons.infrastructure.minio;

import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.modules.commons.domain.model.Document;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
@Testcontainers
class IntegrationMinioFilesOperation {

    /** Voir {@code WithGraphDBContainer#GRAPHDB_IMAGE} : annotation lue par le custom manager Renovate. */
    // renovate: datasource=docker depName=minio/minio
    static final String MINIO_IMAGE = "minio/minio:RELEASE.2024-11-07T00-52-20Z";

    /**
     * Champ statique : un champ d'instance ferait démarrer puis arrêter un conteneur MinIO par
     * méthode de test. Les tests écrivent chacun sous des noms de fichiers distincts, ils peuvent
     * donc partager le même serveur.
     */
    @Container
    static final MinIOContainer container = new MinIOContainer(MINIO_IMAGE);

    @BeforeAll
    static void configureSlf4j() {
        System.setProperty("org.slf4j.simpleLogger.log."+MinioFilesOperation.class.getName(), "debug");
        System.setProperty("slf4j.provider", "org.slf4j.simple.SimpleServiceProvider");
    }

    @Test
    void testWritingThenCheckExistThenCopyThenRead_shouldBeOK() throws MinioException, IOException {
        var nomFichier = "test.txt";
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(container.getS3URL())
                .credentials(container.getUserName(), container.getPassword())
                .build();
        MinioFilesOperation minioFilesOperation = new MinioFilesOperation(minioClient, "metadata");
        createBucket(minioFilesOperation.bucketName(), minioClient);

        Document documentInGestion = new Document("gestion/documents", nomFichier);
        String contenuFichier = "Test";
        minioFilesOperation.write(new ByteArrayInputStream(contenuFichier.getBytes()), documentInGestion);
        assertThat(minioFilesOperation.exists(documentInGestion)).isTrue();

        Document documentInPublication = new Document("publication/documents", nomFichier);
        minioFilesOperation.copy(documentInGestion, documentInPublication);
        assertThat(minioFilesOperation.exists(documentInPublication)).isTrue();

        assertThat(new String(minioFilesOperation.read(documentInGestion).readAllBytes())).isEqualTo(contenuFichier);
    }

    @Test
    void testDelete_shouldRemoveDocument() throws MinioException {
        var nomFichier = "file-to-delete.txt";
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(container.getS3URL())
                .credentials(container.getUserName(), container.getPassword())
                .build();
        MinioFilesOperation minioFilesOperation = new MinioFilesOperation(minioClient, "metadata");
        createBucket(minioFilesOperation.bucketName(), minioClient);

        Document document = new Document("test/path", nomFichier);
        String contenuFichier = "Content to delete";
        minioFilesOperation.write(new ByteArrayInputStream(contenuFichier.getBytes()), document);
        assertThat(minioFilesOperation.exists(document)).isTrue();

        minioFilesOperation.delete(document);
        assertThat(minioFilesOperation.exists(document)).isFalse();
    }

    @Test
    void testExists_shouldReturnFalse_whenDocumentDoesNotExist() throws MinioException {
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(container.getS3URL())
                .credentials(container.getUserName(), container.getPassword())
                .build();
        MinioFilesOperation minioFilesOperation = new MinioFilesOperation(minioClient, "metadata");
        createBucket(minioFilesOperation.bucketName(), minioClient);

        Document nonExistentDocument = new Document("test/path", "non-existent.txt");
        assertThat(minioFilesOperation.exists(nonExistentDocument)).isFalse();
    }

    @Test
    void testRead_shouldThrowException_whenDocumentDoesNotExist() throws MinioException {
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(container.getS3URL())
                .credentials(container.getUserName(), container.getPassword())
                .build();
        MinioFilesOperation minioFilesOperation = new MinioFilesOperation(minioClient, "metadata");
        createBucket(minioFilesOperation.bucketName(), minioClient);

        Document nonExistentDocument = new Document("test/path", "non-existent.txt");
        assertThatThrownBy(() -> minioFilesOperation.read(nonExistentDocument))
                .isInstanceOf(RmesFileException.class)
                .hasMessageContaining("Error reading file");
    }

    @Test
    void testCopy_shouldThrowException_whenSourceDocumentDoesNotExist() throws MinioException {
        MinioClient minioClient = MinioClient
                .builder()
                .endpoint(container.getS3URL())
                .credentials(container.getUserName(), container.getPassword())
                .build();
        MinioFilesOperation minioFilesOperation = new MinioFilesOperation(minioClient, "metadata");
        createBucket(minioFilesOperation.bucketName(), minioClient);

        Document nonExistentSource = new Document("test/path", "non-existent-source.txt");
        Document targetDocument = new Document("test/path", "target.txt");
        assertThatThrownBy(() -> minioFilesOperation.copy(nonExistentSource, targetDocument))
                .isInstanceOf(RmesFileException.class)
                .hasMessageContaining("Error copying file");
    }

    /** Le conteneur étant partagé par la classe, le bucket survit d'une méthode de test à l'autre. */
    private void createBucket(String bucketName, MinioClient minioClient) throws MinioException {
        if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            return;
        }
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

}
