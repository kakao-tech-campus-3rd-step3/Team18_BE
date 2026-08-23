package com.kakaotech.team18.backend_server.domain.clubPopularity.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ClubPopularityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ClubPopularityConfig.class);

    @Test
    @DisplayName("인기 기능 설정 기본값과 UTC Clock을 제공한다")
    void providesDefaultsAndUtcClock() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            ClubPopularityProperties properties = context.getBean(ClubPopularityProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getRecentViewerThreshold()).isEqualTo(10);
            assertThat(properties.getActiveViewerThreshold()).isEqualTo(3);
            assertThat(properties.getRecentViewerWindowHours()).isEqualTo(24);
            assertThat(properties.getFlushBatchSize()).isEqualTo(500);
            assertThat(properties.getTransientRetryDelaysSeconds()).containsExactly(1, 3, 10);
            assertThat(context.getBean(Clock.class).getZone()).isEqualTo(ZoneOffset.UTC);
        });
    }

    @Test
    @DisplayName("외부 설정값을 ClubPopularityProperties에 바인딩한다")
    void bindsExternalProperties() {
        contextRunner
                .withPropertyValues(
                        "club-popularity.enabled=false",
                        "club-popularity.recent-viewer-threshold=12",
                        "club-popularity.flush-batch-size=250")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    ClubPopularityProperties properties = context.getBean(ClubPopularityProperties.class);
                    assertThat(properties.isEnabled()).isFalse();
                    assertThat(properties.getRecentViewerThreshold()).isEqualTo(12);
                    assertThat(properties.getFlushBatchSize()).isEqualTo(250);
                });
    }

    @Test
    @DisplayName("heartbeat 제한 간격이 호출 주기보다 길면 애플리케이션 시작에 실패한다")
    void rejectsInvalidHeartbeatIntervals() {
        contextRunner
                .withPropertyValues(
                        "club-popularity.heartbeat-interval-seconds=30",
                        "club-popularity.heartbeat-min-interval-seconds=30")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("DB 보관 시간이 25시간 미만이면 애플리케이션 시작에 실패한다")
    void rejectsRetentionShorterThanRequired() {
        contextRunner
                .withPropertyValues("club-popularity.retention-hours=24")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("flush 배치 크기가 실행 상한을 넘으면 애플리케이션 시작에 실패한다")
    void rejectsBatchSizeBeyondRunLimit() {
        ClubPopularityProperties properties = new ClubPopularityProperties();
        properties.setFlushBatchSize(501);
        properties.setFlushMaxRecordsPerRun(500);

        assertThat(properties.areBatchSizesWithinRunLimit()).isFalse();
    }

    @Test
    @DisplayName("실패 재시도 횟수와 지연 목록 크기가 다르면 애플리케이션 시작에 실패한다")
    void rejectsIncompleteFailedRetrySchedule() {
        contextRunner
                .withPropertyValues(
                        "club-popularity.failed-record-max-attempts=2",
                        "club-popularity.failed-record-retry-delays-minutes[0]=10",
                        "club-popularity.failed-record-retry-delays-minutes[1]=60",
                        "club-popularity.failed-record-retry-delays-minutes[2]=360")
                .run(context -> assertThat(context).hasFailed());
    }
}
