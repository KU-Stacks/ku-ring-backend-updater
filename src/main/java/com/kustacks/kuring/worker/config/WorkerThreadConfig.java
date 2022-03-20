package com.kustacks.kuring.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class WorkerThreadConfig {

    @Bean
    public ThreadPoolTaskExecutor kuisNoticeUpdaterThreadTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setKeepAliveSeconds(10);
        taskExecutor.setCorePoolSize(0);
        taskExecutor.setMaxPoolSize(1);
        taskExecutor.setQueueCapacity(17);
        taskExecutor.setThreadNamePrefix("kuisNot-updt-");
        taskExecutor.initialize();
        // TODO: Callable과 Future<>를 이용한 예외처리?
        return taskExecutor;
    }

    @Bean
    public ThreadPoolTaskExecutor etcNoticeUpdaterThreadTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setKeepAliveSeconds(10);
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(116);
        taskExecutor.setThreadNamePrefix("etcNot-updt-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public ThreadPoolTaskExecutor staffUpdaterThreadTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setKeepAliveSeconds(10);
        taskExecutor.setCorePoolSize(0);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(55);
        taskExecutor.setThreadNamePrefix("stf-updt-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public ThreadPoolTaskExecutor userUpdaterThreadTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(0);
        taskExecutor.setMaxPoolSize(1);
        taskExecutor.setQueueCapacity(1);
        taskExecutor.setThreadNamePrefix("usr-updt-");
        taskExecutor.initialize();
        return taskExecutor;
    }
}
