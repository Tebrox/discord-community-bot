package de.tebrox.communitybot.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SchedulingConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService sseScheduler() {
        return Executors.newScheduledThreadPool(1);
    }
}