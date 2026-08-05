package com.kakaotech.team18.backend_server.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    LettuceClientConfigurationBuilderCustomizer redisCommandTimeoutCustomizer(
            @Value("${app.redis.command-timeout:3s}") Duration commandTimeout) {
        // Redis 장애 시 Lettuce 기본 1분 대기를 막고 빠르게 우회 경로로 전환한다.
        return builder -> builder.commandTimeout(commandTimeout);
    }
}
