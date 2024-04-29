package fr.insee.rmes.config;

import fr.insee.rmes.bauhaus_services.FileSystemOperation;
import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.MinioFilesOperation;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "minio")
    public FilesOperations filesMinioOperations(MinioClient minioClient, @Value("${minio.bucket.name}") String bucketName) {
        return new MinioFilesOperation(minioClient, bucketName);
    }


    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "filesystem")
    public FilesOperations filesSytemOperations() {
        return new FileSystemOperation();
    }

}