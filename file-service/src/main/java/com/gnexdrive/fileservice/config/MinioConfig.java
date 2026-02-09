package com.gnexdrive.fileservice.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO configuration
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
@RequiredArgsConstructor
public class MinioConfig {

    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        log.info("Creating MinIO client for URL: {}", url);
        
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Initialize bucket on startup if it doesn't exist
     */
    @Bean
    public CommandLineRunner initializeBucket(MinioClient minioClient) {
        return args -> {
            try {
                boolean bucketExists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build()
                );

                if (!bucketExists) {
                    log.info("Creating MinIO bucket: {}", bucketName);
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucketName).build()
                    );
                    log.info("MinIO bucket created successfully: {}", bucketName);
                } else {
                    log.info("MinIO bucket already exists: {}", bucketName);
                }
            } catch (Exception e) {
                log.error("Failed to initialize MinIO bucket: {}", e.getMessage());
            }
        };
    }
}
