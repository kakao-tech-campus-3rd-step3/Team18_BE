package com.kakaotech.team18.backend_server.domain.statistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 지원자 통계 정책 설정값.
 *
 * @param minTotalApplicants 공개 통계 재식별 방지 최소 공개 기준. 전체 지원자 수가 이 값 미만이면 분포가 소수라
 *                           재식별 위험이 커지므로 공개 통계를 비공개(masked) 처리한다. 관리자 통계는 이 값과
 *                           무관하게 원본을 노출한다. 기준은 전체 지원자 수에만 걸리고 개별 버킷에는 걸지 않는다.
 * @param cacheTtlSeconds    공개 통계 캐시(Redis) TTL(초). 조회 시 캐시에 없으면 실시간 계산해 이 시간만큼 캐시한다.
 *                           관리자 통계는 캐시를 거치지 않는다. 스케줄러 없이 요청 시점에만 채우는 cache-aside 방식이다.
 */
@ConfigurationProperties(prefix = "statistics")
public record StatisticsProperties(
        @DefaultValue("3") int minTotalApplicants,
        @DefaultValue("60") long cacheTtlSeconds
) {
}
