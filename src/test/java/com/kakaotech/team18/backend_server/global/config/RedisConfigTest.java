package com.kakaotech.team18.backend_server.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;

class RedisConfigTest {

    @Test
    void redisCommandTimeoutCustomizerUsesConfiguredTimeout() {
        RedisConfig config = new RedisConfig();

        var builder = LettuceClientConfiguration.builder();
        config.redisCommandTimeoutCustomizer("3s").customize(builder);

        assertThat(builder.build().getCommandTimeout()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void appliesThreeSecondDefaultWhenPropertyIsAbsent() {
        new ApplicationContextRunner()
                .withUserConfiguration(RedisConfig.class)
                .run(context -> {
                    LettuceClientConfigurationBuilderCustomizer customizer =
                            context.getBean(LettuceClientConfigurationBuilderCustomizer.class);
                    var builder = LettuceClientConfiguration.builder();
                    customizer.customize(builder);
                    assertThat(builder.build().getCommandTimeout()).isEqualTo(Duration.ofSeconds(3));
                });
    }
}
