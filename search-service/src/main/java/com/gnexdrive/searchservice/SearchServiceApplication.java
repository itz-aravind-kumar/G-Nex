package com.gnexdrive.searchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Search Service Application
 * Provides fast file search using Elasticsearch and Redis cache
 */
@SpringBootApplication
@EnableKafka
@EnableCaching
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
