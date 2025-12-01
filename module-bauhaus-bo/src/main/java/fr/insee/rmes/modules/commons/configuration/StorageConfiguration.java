package fr.insee.rmes.modules.commons.configuration;

import fr.insee.rmes.modules.commons.infrastructure.filessystem.FileSystemOperation;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.commons.infrastructure.minio.MinioFilesOperation;
import fr.insee.rmes.modules.commons.infrastructure.minio.MinioConfig;
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
    public FilesOperations filesMinioOperations(MinioConfig minioConfig) {
        return new MinioFilesOperation(minioClient(minioConfig), minioConfig.bucketName());
    }

    private MinioClient minioClient(MinioConfig minioConfig) {
        return MinioClient.builder()
                .endpoint(minioConfig.url())
                .credentials(minioConfig.accessName(), minioConfig.secretKey())
                .build();
    }
}