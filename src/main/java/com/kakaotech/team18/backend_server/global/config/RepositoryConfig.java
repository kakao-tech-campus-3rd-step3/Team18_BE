package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.domain.auth.repository.RefreshTokenRepository;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.kakaotech.team18.backend_server.domain",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RefreshTokenRepository.class
        )
)
@EnableRedisRepositories(
        basePackages = "com.kakaotech.team18.backend_server.domain",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RefreshTokenRepository.class
        )
)
public class RepositoryConfig {
}
