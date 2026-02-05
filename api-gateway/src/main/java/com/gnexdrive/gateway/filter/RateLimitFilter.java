package com.gnexdrive.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Rate limiting filter configuration
 * Provides different strategies for rate limiting keys
 */
@Slf4j
@Configuration
public class RateLimitFilter {

    /**
     * User-based rate limiting key resolver
     * Uses X-User-Id header set by JWT authentication filter
     * Limits: 100 requests per minute per user
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Extract user ID from header (set by JwtAuthenticationFilter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            if (userId != null && !userId.isEmpty()) {
                log.debug("Rate limiting key (user): {}", userId);
                return Mono.just(userId);
            }
            
            // Fallback to IP address if no user ID
            String ip = getClientIp(exchange);
            log.debug("Rate limiting key (fallback to IP): {}", ip);
            return Mono.just(ip);
        };
    }

    /**
     * IP-based rate limiting key resolver
     * Uses client IP address for rate limiting
     * Limits: 200 requests per minute per IP
     * Useful for public endpoints or as fallback
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = getClientIp(exchange);
            log.debug("Rate limiting key (IP): {}", ip);
            return Mono.just(ip);
        };
    }

    /**
     * Path-based rate limiting key resolver
     * Uses request path for rate limiting
     * Useful for limiting specific endpoints
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            log.debug("Rate limiting key (path): {}", path);
            return Mono.just(path);
        };
    }

    /**
     * Extract client IP address from request
     * Handles X-Forwarded-For header for proxied requests
     */
    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        // Check X-Forwarded-For header first (for proxied requests)
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return forwardedFor.split(",")[0].trim();
        }
        
        // Fallback to remote address
        return Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress(),
                "Remote address not available"
        ).getAddress().getHostAddress();
    }
}
