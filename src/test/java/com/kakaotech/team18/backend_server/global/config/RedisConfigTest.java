package com.kakaotech.team18.backend_server.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;

class RedisConfigTest {

    @Test
    void redisCommandTimeoutCustomizerUsesConfiguredTimeout() {
        RedisConfig config = new RedisConfig();

        var builder = LettuceClientConfiguration.builder();
        config.redisCommandTimeoutCustomizer(Duration.ofSeconds(3)).customize(builder);

        assertThat(builder.build().getCommandTimeout()).isEqualTo(Duration.ofSeconds(3));
    }
}
