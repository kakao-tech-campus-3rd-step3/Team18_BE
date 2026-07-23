package com.kakaotech.team18.backend_server.global.scheduler;

import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsPrecomputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 지원자 통계를 시간 주기로 미리 계산합니다.
 * <p>
 * 집계 주기가 곧 통계의 신선도다. 지원자 수를 기준으로 집계를 실행하지 않는 이유는, 그렇게 하면 마감 직전
 * 몰림 구간에서 집계 횟수가 지원 건수에 비례해 늘어나 사전 계산을 도입한 이유가 사라지기 때문이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsPrecomputeScheduler {

    private final StatisticsPrecomputeService precomputeService;

    /**
     * 집계 주기는 {@code statistics.precompute.cron} 설정으로 조정한다. 기본값은 10분이다.
     * <p>
     * {@code fixedDelay}가 아니라 cron을 쓰는 이유는 여러 인스턴스가 같은 시각에 깨어나게 해서, 선점 잠금이
     * 실제로 경합을 걸러내는지 예측 가능하게 만들기 위해서다.
     */
    @Scheduled(cron = "${statistics.precompute.cron:0 */10 * * * *}")
    public void precomputeStatistics() {
        try {
            log.debug("지원자 통계 사전 계산 시작");
            int updated = precomputeService.precomputeAll();
            log.debug("지원자 통계 사전 계산 종료. 갱신={}", updated);
        } catch (Exception e) {
            // 스케줄러에서 예외가 새어 나가면 다음 주기가 실행되지 않을 수 있다.
            log.error("지원자 통계 사전 계산 중 예외 발생", e);
        }
    }
}
