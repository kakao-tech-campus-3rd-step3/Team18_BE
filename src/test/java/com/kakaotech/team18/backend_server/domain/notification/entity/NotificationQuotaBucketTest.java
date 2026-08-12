package com.kakaotech.team18.backend_server.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationQuotaBucketTest {

    @Test
    void reservesOnlyUpToConfiguredLimit() {
        NotificationQuotaBucket bucket = NotificationQuotaBucket.hourly(
                NotificationChannel.SMS,
                LocalDateTime.of(2026, 8, 12, 13, 0)
        );

        assertThat(bucket.tryReserve(2)).isTrue();
        assertThat(bucket.tryReserve(2)).isTrue();
        assertThat(bucket.tryReserve(2)).isFalse();
        assertThat(bucket.getRequestCount()).isEqualTo(2);
    }
}
