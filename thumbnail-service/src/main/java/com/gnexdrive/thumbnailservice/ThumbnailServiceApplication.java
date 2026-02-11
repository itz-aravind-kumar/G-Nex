package com.gnexdrive.thumbnailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Thumbnail Service Application
 * Asynchronous thumbnail generation service
 */
@SpringBootApplication(scanBasePackages = {"com.gnexdrive.thumbnailservice", "com.gnexdrive.common"})
@EnableKafka
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class ThumbnailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThumbnailServiceApplication.class, args);
    }
}
