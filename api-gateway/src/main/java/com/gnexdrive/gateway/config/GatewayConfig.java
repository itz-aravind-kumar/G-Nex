package com.gnexdrive.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Gateway configuration for circuit breaker and timeouts
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * Configure circuit breaker defaults
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        log.info("Configuring Circuit Breaker defaults");
        
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        // Percentage of failures before opening circuit
                        .failureRateThreshold(50)
                        // Number of calls to calculate failure rate
                        .slidingWindowSize(10)
                        // Minimum calls before circuit can open
                        .minimumNumberOfCalls(5)
                        // Time in open state before half-open
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        // Calls in half-open state
                        .permittedNumberOfCallsInHalfOpenState(3)
                        // Automatically transition from open to half-open
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        // Timeout for downstream calls
                        .timeoutDuration(Duration.ofSeconds(10))
                        .build())
                .build());
    }
}
