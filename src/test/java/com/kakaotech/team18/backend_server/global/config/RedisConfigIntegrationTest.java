package com.kakaotech.team18.backend_server.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;

class RedisConfigIntegrationTest {

    @Test
    void appliesConfiguredTimeoutThroughSpringPropertyBinding() {
        new ApplicationContextRunner()
                .withPropertyValues("spring.data.redis.timeout=2s")
                .withUserConfiguration(RedisConfig.class)
                .run(context -> {
                    LettuceClientConfigurationBuilderCustomizer customizer =
                            context.getBean(LettuceClientConfigurationBuilderCustomizer.class);
                    var builder = LettuceClientConfiguration.builder();
                    customizer.customize(builder);
                    assertThat(builder.build().getCommandTimeout()).isEqualTo(Duration.ofSeconds(2));
                });
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
