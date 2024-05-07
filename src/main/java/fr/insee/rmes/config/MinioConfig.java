package fr.insee.rmes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioConfig (String url, String accessName, String secretKey, String bucketName){}

