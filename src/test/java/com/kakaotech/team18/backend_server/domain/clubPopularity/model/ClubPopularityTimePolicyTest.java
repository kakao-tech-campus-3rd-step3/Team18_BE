package com.kakaotech.team18.backend_server.domain.clubPopularity.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClubPopularityTimePolicyTest {

    private static final Instant NOW = Instant.parse("2026-08-02T00:00:00Z");

    private ClubPopularityTimePolicy timePolicy;

    @BeforeEach
    void setUp() {
        ClubPopularityProperties properties = new ClubPopularityProperties();
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        timePolicy = new ClubPopularityTimePolicy(properties, fixedClock);
    }

    @Test
    @DisplayName("한 작업에서 사용할 현재 시각을 고정 UTC Clock에서 얻는다")
    void getsCurrentInstantFromInjectedClock() {
        assertThat(timePolicy.currentInstant()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("정확히 24시간이 지난 조회자는 최근 조회자에서 제외한다")
    void recentViewerUsesExclusive24HourCutoff() {
        assertThat(timePolicy.isRecentViewer(NOW.minusSeconds(24 * 60 * 60), NOW)).isFalse();
        assertThat(timePolicy.isRecentViewer(NOW.minusSeconds(24 * 60 * 60).plusMillis(1), NOW)).isTrue();
        assertThat(timePolicy.isRecentViewer(NOW.plusMillis(1), NOW)).isFalse();
    }

    @Test
    @DisplayName("정확히 3분이 지난 사용자는 활성 사용자에서 제외한다")
    void activeViewerUsesExclusiveThreeMinuteCutoff() {
        assertThat(timePolicy.isActiveViewer(NOW.minusSeconds(180), NOW)).isFalse();
        assertThat(timePolicy.isActiveViewer(NOW.minusSeconds(180).plusMillis(1), NOW)).isTrue();
        assertThat(timePolicy.isActiveViewer(NOW.plusMillis(1), NOW)).isFalse();
    }

    @Test
    @DisplayName("정확히 25시간이 지난 DB 기록은 삭제 대상이다")
    void dbRetentionUsesInclusive25HourCutoff() {
        assertThat(timePolicy.shouldDeleteViewRecord(NOW.minusSeconds(25 * 60 * 60), NOW)).isTrue();
        assertThat(timePolicy.shouldDeleteViewRecord(NOW.minusSeconds(25 * 60 * 60).plusMillis(1), NOW)).isFalse();
    }
}
