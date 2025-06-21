package com.maijin.captcha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // 核心线程池大小
        executor.setMaxPoolSize(50);   // 最大线程池大小
        executor.setQueueCapacity(100); // 队列容量
        executor.initialize();         // 初始化线程池
        return executor;
    }
}
