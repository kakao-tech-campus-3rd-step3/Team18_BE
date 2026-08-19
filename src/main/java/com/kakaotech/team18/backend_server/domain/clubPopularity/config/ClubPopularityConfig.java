package com.kakaotech.team18.backend_server.domain.clubPopularity.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ClubPopularityProperties.class)
public class ClubPopularityConfig {

    @Bean
    public Clock clubPopularityClock() {
        // Redis 점수와 DB 시각 계산의 기준을 UTC 하나로 통일한다.
        return Clock.systemUTC();
    }
}
