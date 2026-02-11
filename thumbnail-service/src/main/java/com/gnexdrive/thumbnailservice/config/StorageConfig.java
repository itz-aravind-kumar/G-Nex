package com.gnexdrive.thumbnailservice.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Object Storage Configuration (MinIO/S3)
 */
@Configuration
public class StorageConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "minio")
    public static class MinioProperties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private BucketConfig bucket;
        private int presignedUrlTtl;

        @Data
        public static class BucketConfig {
            private String thumbnails;
            private String files;
        }
    }
}
