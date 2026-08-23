package com.kakaotech.team18.backend_server.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.convert.DurationStyle;

@Configuration
public class RedisConfig {

    private static final Duration MAX_COMMAND_TIMEOUT = Duration.ofSeconds(3);

    @Bean
    LettuceClientConfigurationBuilderCustomizer redisCommandTimeoutCustomizer(
            @Value("${spring.data.redis.timeout:3s}") String commandTimeoutValue) {
        // Redis 장애 시 Lettuce 기본 1분 대기를 막고 빠르게 우회 경로로 전환한다.
        Duration parsedTimeout = DurationStyle.detectAndParse(commandTimeoutValue);
        if (parsedTimeout.isZero() || parsedTimeout.isNegative()) {
            throw new IllegalArgumentException("Redis command timeout must be positive");
        }
        Duration commandTimeout = parsedTimeout.compareTo(MAX_COMMAND_TIMEOUT) > 0
                ? MAX_COMMAND_TIMEOUT
                : parsedTimeout;
        return builder -> builder.commandTimeout(commandTimeout);
    }
}
