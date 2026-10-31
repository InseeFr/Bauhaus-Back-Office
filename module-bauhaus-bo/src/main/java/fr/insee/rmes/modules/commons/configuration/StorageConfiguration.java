package fr.insee.rmes.modules.commons.configuration;

import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.commons.healthcheck.DependencyProbe;
import fr.insee.rmes.modules.commons.infrastructure.filessystem.FileSystemOperation;
import fr.insee.rmes.modules.commons.infrastructure.minio.MinioFilesOperation;
import fr.insee.rmes.modules.commons.infrastructure.minio.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfiguration {

    @Bean
    @Profile("! s3")
    public FilesOperations filesSytemOperations() {
        return new FileSystemOperation();
    }

    @Bean
    @Profile("s3")
    public FilesOperations filesMinioOperations(MinioClient minioClient, MinioProperties minioProperties) {
        return new MinioFilesOperation(minioClient, minioProperties.bucketName());
    }

    /** Sonde du healthcheck : le bucket des documents doit exister et MinIO répondre. */
    @Bean
    @Profile("s3")
    public DependencyProbe minioProbe(MinioClient minioClient, MinioProperties minioProperties) {
        String bucketName = minioProperties.bucketName();
        return new DependencyProbe("MinIO", () -> {
            if (!minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build())) {
                throw new IllegalStateException("Bucket " + bucketName + " not found");
            }
        });
    }

    @Bean
    @Profile("s3")
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.url())
                .credentials(minioProperties.accessName(), minioProperties.secretKey())
                .build();
    }
}
