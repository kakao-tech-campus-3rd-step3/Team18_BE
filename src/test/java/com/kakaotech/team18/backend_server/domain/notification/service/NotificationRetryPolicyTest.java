package com.kakaotech.team18.backend_server.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationRetryPolicyTest {

    private final NotificationRetryPolicy policy = new NotificationRetryPolicy(5, 60, 600);

    @Test
    void appliesExponentialBackoffUpToMaximumDelay() {
        LocalDateTime failedAt = LocalDateTime.of(2026, 8, 12, 13, 0);

        assertThat(policy.nextRetryAt(failedAt, 1)).isEqualTo(failedAt.plusSeconds(60));
        assertThat(policy.nextRetryAt(failedAt, 2)).isEqualTo(failedAt.plusSeconds(120));
        assertThat(policy.nextRetryAt(failedAt, 5)).isEqualTo(failedAt.plusSeconds(600));
        assertThat(policy.nextRetryAt(failedAt, 10)).isEqualTo(failedAt.plusSeconds(600));
    }

    @Test
    void stopsAtMaximumAttemptsExceptForHourlyQuotaDeferral() {
        assertThat(policy.exhausted(4, "TEMPORARY_FAILURE")).isFalse();
        assertThat(policy.exhausted(5, "TEMPORARY_FAILURE")).isTrue();
        assertThat(policy.exhausted(100, "SOLAPI_HOURLY_QUOTA_EXCEEDED")).isFalse();
    }
}
