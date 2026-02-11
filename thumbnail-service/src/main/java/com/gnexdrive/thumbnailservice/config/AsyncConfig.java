package com.gnexdrive.thumbnailservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async Processing Configuration
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${thumbnail.worker.pool-size:10}")
    private int poolSize;

    @Value("${thumbnail.worker.queue-capacity:100}")
    private int queueCapacity;

    @Bean(name = "thumbnailExecutor")
    public Executor thumbnailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("thumbnail-worker-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("Initialized thumbnail executor with pool-size={}, queue-capacity={}", poolSize, queueCapacity);
        return executor;
    }
}
