package com.kakaotech.team18.backend_server.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.convert.DurationStyle;

@Configuration
public class RedisConfig {

    @Bean
    LettuceClientConfigurationBuilderCustomizer redisCommandTimeoutCustomizer(
            @Value("${spring.data.redis.timeout:3s}") String commandTimeoutValue) {
        // Redis 장애 시 Lettuce 기본 1분 대기를 막고 빠르게 우회 경로로 전환한다.
        Duration commandTimeout = DurationStyle.detectAndParse(commandTimeoutValue);
        return builder -> builder.commandTimeout(commandTimeout);
    }
}
