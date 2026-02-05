package com.gnexdrive.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

/**
 * API Gateway Application
 * Central entry point for all client requests
 */
@Slf4j
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        log.info("Starting API Gateway Application...");
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("API Gateway started successfully");
    }

    /**
     * Custom route configuration with circuit breaker and retry policies
     * This provides programmatic routing in addition to YAML configuration
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring custom routes");
        
        return builder.routes()
                // File Service Routes with Circuit Breaker
                .route("file-service-with-cb", r -> r
                        .path("/api/v1/files/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("fileServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/file-service")
                                )
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)
                                )
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(null) // Uses default Redis rate limiter
                                )
                        )
                        .uri("lb://file-service")
                )
                
                // Metadata Service Routes
                .route("metadata-service-with-cb", r -> r
                        .path("/api/v1/metadata/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("metadataServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/metadata-service")
                                )
                                .retry(retryConfig -> retryConfig.setRetries(3))
                        )
                        .uri("lb://metadata-service")
                )
                
                // Search Service Routes
                .route("search-service-with-cb", r -> r
                        .path("/api/v1/search/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("searchServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/search-service")
                                )
                                .retry(retryConfig -> retryConfig.setRetries(2))
                        )
                        .uri("lb://search-service")
                )
                
                // Activity Service Routes
                .route("activity-service-with-cb", r -> r
                        .path("/api/v1/activities/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("activityServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/activity-service")
                                )
                                .retry(retryConfig -> retryConfig.setRetries(2))
                        )
                        .uri("lb://activity-service")
                )
                
                .build();
    }
}
