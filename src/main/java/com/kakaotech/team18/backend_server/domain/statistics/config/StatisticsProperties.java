package com.kakaotech.team18.backend_server.domain.statistics.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 지원자 통계 관련 설정.
 * <p>
 * 재식별 방지 임계값과 공개 기준을 코드가 아닌 설정으로 분리해, 운영 중 모집 규모에 맞춰 조정할 수 있게 한다.
 *
 * @param masking    재식별 방지 관련 기준
 * @param department 학과 dimension 관련 기준
 * @param precompute 스케줄러 사전 계산 관련 기준
 */
@ConfigurationProperties(prefix = "statistics")
public record StatisticsProperties(

        @DefaultValue Masking masking,
        @DefaultValue Department department,
        @DefaultValue AdmissionYear admissionYear,
        @DefaultValue Precompute precompute
) {

    /**
     * @param bucketThreshold 이 값보다 적은 지원자를 가진 버킷은 개별 노출하지 않고 '기타'로 합친다.
     *                        통계 공표에서 소수 집단 재식별을 막는 관례값이 5다.
     * @param minPublicTotal  누적 지원자가 이 값보다 적으면 속성 분포를 아예 공개하지 않는다.
     *                        지원자가 극소수일 때는 어떤 마스킹을 해도 조합으로 개인이 드러날 수 있다.
     */
    public record Masking(
            @DefaultValue("5") int bucketThreshold,
            @DefaultValue("10") int minPublicTotal
    ) {
    }

    /**
     * @param topN 학과는 값 종류가 많으므로 상위 N개만 노출하고 나머지는 '기타'로 합친다.
     */
    public record Department(
            @DefaultValue("5") int topN
    ) {
    }

    /**
     * @param minYear 유효한 입학연도의 하한. 이보다 이르거나 기준 연도를 넘는 학번은 정상적인 입학연도로 볼 수
     *                없으므로 '미입력'으로 분류한다. 오타(예: {@code 991234})가 1999학번으로 집계되는 것을 막는다.
     */
    public record AdmissionYear(
            @DefaultValue("1990") int minYear
    ) {
    }

    /**
     * @param enabled     사전 계산 사용 여부. 끄면 조회 시점에 집계한다(개발·테스트 편의).
     * @param publishStep 직전 공개 대비 지원자가 이만큼 늘어야 캐시를 갱신한다. 1이면 한 명만 늘어도 갱신한다.
     * @param cacheTtl    캐시 엔트리 TTL. 집계 주기보다 충분히 길게 잡아 만료로 인한 빈 응답을 막는다.
     * @param lockTtl     다중 인스턴스 선점 잠금의 TTL. 집계가 이 시간을 넘기면 다른 인스턴스가 이어받는다.
     */
    public record Precompute(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("1") int publishStep,
            @DefaultValue("PT2H") Duration cacheTtl,
            @DefaultValue("PT5M") Duration lockTtl
    ) {
    }
}
