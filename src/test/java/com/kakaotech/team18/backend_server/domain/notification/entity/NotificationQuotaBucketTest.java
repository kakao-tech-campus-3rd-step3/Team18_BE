package com.kakaotech.team18.backend_server.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationQuotaPeriod;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessageType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationQuotaBucketTest {

    @Test
    void reservesOnlyUpToConfiguredLimit() {
        NotificationQuotaBucket bucket = NotificationQuotaBucket.startedAt(
                NotificationChannel.SMS,
                NotificationQuotaPeriod.HOUR,
                LocalDateTime.of(2026, 8, 12, 13, 0)
        );

        assertThat(bucket.tryReserve(2, BigDecimal.ZERO, SmsMessageType.SMS, new BigDecimal("20"))).isTrue();
        assertThat(bucket.tryReserve(2, BigDecimal.ZERO, SmsMessageType.LMS, new BigDecimal("50"))).isTrue();
        assertThat(bucket.tryReserve(2, BigDecimal.ZERO, SmsMessageType.SMS, new BigDecimal("20"))).isFalse();
        assertThat(bucket.getRequestCount()).isEqualTo(2);
        assertThat(bucket.getSmsCount()).isEqualTo(1);
        assertThat(bucket.getLmsCount()).isEqualTo(1);
        assertThat(bucket.getEstimatedCost()).isEqualByComparingTo("70");
        assertThat(bucket.claimReachedAlertPercent(2)).isEqualTo(100);
        assertThat(bucket.claimReachedAlertPercent(2)).isZero();
    }
}
