package com.kakaotech.team18.backend_server.domain.clubPopularity.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import org.junit.jupiter.api.Test;

class ClubPopularityMetricsTest {

    @Test
    void recordsOperationalMetricsWithoutViewerIdentifiers() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ClubPopularityMetrics metrics = new ClubPopularityMetrics(registry, new ClubPopularityProperties());
        metrics.registerGauges();
        metrics.recordApi("views", "redis_error");
        metrics.recordRedisError("views");
        metrics.setPendingState(12, 7200);
        metrics.setRecoveryInProgress(true);
        metrics.setEnabled(false);

        assertThat(registry.get("club.popularity.api.requests").tag("api", "views").counter().count()).isEqualTo(1);
        assertThat(registry.get("club.popularity.pending.count").gauge().value()).isEqualTo(12);
        assertThat(registry.get("club.popularity.recovery.in.progress").gauge().value()).isEqualTo(1);
        assertThat(registry.get("club.popularity.enabled").gauge().value()).isEqualTo(0);
        assertThat(registry.getMeters()).allSatisfy(meter ->
                assertThat(meter.getId().getTags()).noneMatch(tag ->
                        tag.getKey().matches("clubId|userId|anonymousId|failureId")));
    }
}
