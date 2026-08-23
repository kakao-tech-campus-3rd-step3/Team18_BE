package com.kakaotech.team18.backend_server.domain.statistics.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 통계 도메인 설정. {@link StatisticsProperties}를 빈으로 등록한다.
 */
@Configuration
@EnableConfigurationProperties(StatisticsProperties.class)
public class StatisticsConfig {
}
