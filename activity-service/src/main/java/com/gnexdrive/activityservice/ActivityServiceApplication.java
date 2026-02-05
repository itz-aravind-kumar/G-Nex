package com.gnexdrive.activityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Activity Service Application
 * Tracks file-related activities asynchronously
 */
@SpringBootApplication
@EnableKafka
@EnableJpaAuditing
public class ActivityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityServiceApplication.class, args);
    }
}
