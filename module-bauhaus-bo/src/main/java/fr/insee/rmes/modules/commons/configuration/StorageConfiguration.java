package fr.insee.rmes.modules.commons.configuration;

import fr.insee.rmes.Config;
import fr.insee.rmes.bauhaus_services.FileSystemOperation;
import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.MinioFilesOperation;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfiguration {

    @Bean
    @Profile("s3")
    public FilesOperations filesMinioOperations(MinioConfig minioConfig) {
        return new MinioFilesOperation(minioClient(minioConfig), minioConfig.bucketName(),minioConfig.directoryGestion(),minioConfig.directoryPublication());
    }

    private MinioClient minioClient(MinioConfig minioConfig) {
        return MinioClient.builder()
                .endpoint(minioConfig.url())
                .credentials(minioConfig.accessName(), minioConfig.secretKey())
                .build();
    }


    @Bean
    @Profile("! s3")
    public FilesOperations filesSytemOperations(Config config) {
        return new FileSystemOperation(config);
    }

}