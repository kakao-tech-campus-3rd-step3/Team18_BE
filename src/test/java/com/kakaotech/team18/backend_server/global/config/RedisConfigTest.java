package com.kakaotech.team18.backend_server.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;

class RedisConfigTest {

    @Test
    void redisCommandTimeoutCustomizerUsesConfiguredTimeout() {
        RedisConfig config = new RedisConfig();

        var builder = LettuceClientConfiguration.builder();
        config.redisCommandTimeoutCustomizer("2s").customize(builder);

        assertThat(builder.build().getCommandTimeout()).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void capsConfiguredTimeoutAtThreeSeconds() {
        RedisConfig config = new RedisConfig();

        var builder = LettuceClientConfiguration.builder();
        config.redisCommandTimeoutCustomizer("10s").customize(builder);

        assertThat(builder.build().getCommandTimeout()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void rejectsNonPositiveTimeout() {
        RedisConfig config = new RedisConfig();

        assertThatThrownBy(() -> config.redisCommandTimeoutCustomizer("0s"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Redis command timeout must be positive");
    }
}
