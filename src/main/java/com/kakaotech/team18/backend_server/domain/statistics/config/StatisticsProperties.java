package com.kakaotech.team18.backend_server.domain.statistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 지원자 통계 정책 설정값.
 *
 * @param minBucketSize 공개 통계 재식별 방지 최소 공개 기준(k). 버킷 인원이 {@code 0 < count < k}이면
 *                      해당 버킷의 count/ratio를 마스킹한다. {@code k <= 1}이면 마스킹하지 않는다(관리자 경로).
 *                      count가 0인 버킷(예: 시계열 zero-fill)은 식별 대상이 없으므로 마스킹하지 않는다.
 */
@ConfigurationProperties(prefix = "statistics")
public record StatisticsProperties(
        @DefaultValue("5") int minBucketSize
) {
}
