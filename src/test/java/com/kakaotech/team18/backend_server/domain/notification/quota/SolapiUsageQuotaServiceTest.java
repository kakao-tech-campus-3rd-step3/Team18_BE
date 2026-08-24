package com.kakaotech.team18.backend_server.domain.notification.quota;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationQuotaBucket;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationQuotaBucketRepository;
import com.kakaotech.team18.backend_server.domain.notification.sms.PreparedSmsMessage;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessageType;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationQuotaPeriod;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolapiUsageQuotaServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-08-12T04:30:00Z");
    private static final LocalDateTime HOUR_START = LocalDateTime.of(2026, 8, 12, 13, 0);

    @Mock
    private NotificationQuotaBucketRepository repository;

    private SolapiUsageQuotaService service;

    @BeforeEach
    void setUp() {
        service = new SolapiUsageQuotaService(
                repository,
                2,
                3,
                10,
                new BigDecimal("1000"),
                Clock.fixed(NOW, SEOUL)
        );
    }

    @Test
    void reservesHourDayAndMonthUsageAtomically() {
        when(repository.findByChannelAndPeriodAndBucketStartedAt(any(), any(), any()))
                .thenReturn(Optional.empty());

        service.reserve(message(SmsMessageType.LMS, "50"));

        ArgumentCaptor<NotificationQuotaBucket> captor =
                ArgumentCaptor.forClass(NotificationQuotaBucket.class);
        verify(repository, org.mockito.Mockito.times(3)).saveAndFlush(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(NotificationQuotaBucket::getPeriod)
                .containsExactly(
                        NotificationQuotaPeriod.HOUR,
                        NotificationQuotaPeriod.DAY,
                        NotificationQuotaPeriod.MONTH
                );
        assertThat(captor.getAllValues())
                .extracting(NotificationQuotaBucket::getBucketStartedAt)
                .containsExactly(
                        LocalDateTime.of(2026, 8, 12, 13, 0),
                        LocalDateTime.of(2026, 8, 12, 0, 0),
                        LocalDateTime.of(2026, 8, 1, 0, 0)
                );
        assertThat(captor.getAllValues()).allSatisfy(bucket -> {
            assertThat(bucket.getRequestCount()).isEqualTo(1);
            assertThat(bucket.getLmsCount()).isEqualTo(1);
            assertThat(bucket.getEstimatedCost()).isEqualByComparingTo("50");
        });
    }

    @Test
    void rejectsAtHourlyLimitAndReturnsNextHour() {
        NotificationQuotaBucket bucket = NotificationQuotaBucket.startedAt(
                NotificationChannel.SMS,
                NotificationQuotaPeriod.HOUR,
                HOUR_START
        );
        bucket.tryReserve(2, BigDecimal.ZERO, SmsMessageType.SMS, BigDecimal.ZERO);
        bucket.tryReserve(2, BigDecimal.ZERO, SmsMessageType.SMS, BigDecimal.ZERO);
        when(repository.findByChannelAndPeriodAndBucketStartedAt(
                NotificationChannel.SMS,
                NotificationQuotaPeriod.HOUR,
                HOUR_START
        )).thenReturn(Optional.of(bucket));

        assertThatThrownBy(() -> service.reserve(message(SmsMessageType.SMS, "20")))
                .isInstanceOfSatisfying(SolapiQuotaExceededException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo("SOLAPI_HOURLY_QUOTA_EXCEEDED");
                    assertThat(exception.getRetryAt())
                            .isEqualTo(LocalDateTime.of(2026, 8, 12, 14, 0));
                });
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMonthlyEstimatedCostLimit() {
        LocalDateTime monthStart = LocalDateTime.of(2026, 8, 1, 0, 0);
        NotificationQuotaBucket monthly = NotificationQuotaBucket.startedAt(
                NotificationChannel.SMS,
                NotificationQuotaPeriod.MONTH,
                monthStart
        );
        monthly.tryReserve(10, new BigDecimal("1000"), SmsMessageType.LMS, new BigDecimal("980"));
        when(repository.findByChannelAndPeriodAndBucketStartedAt(any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == NotificationQuotaPeriod.MONTH
                        ? Optional.of(monthly)
                        : Optional.empty());

        assertThatThrownBy(() -> service.reserve(message(SmsMessageType.LMS, "50")))
                .isInstanceOfSatisfying(SolapiQuotaExceededException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo("SOLAPI_MONTHLY_QUOTA_EXCEEDED");
                    assertThat(exception.getRetryAt()).isEqualTo(LocalDateTime.of(2026, 9, 1, 0, 0));
                });
    }

    @Test
    void rejectsInvalidLimits() {
        assertThatThrownBy(() -> new SolapiUsageQuotaService(
                repository,
                0,
                1,
                1,
                BigDecimal.ZERO,
                Clock.fixed(NOW, SEOUL)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private PreparedSmsMessage message(SmsMessageType type, String cost) {
        return new PreparedSmsMessage(
                "01012345678",
                "결과 안내",
                type == SmsMessageType.LMS ? "동아리 지원 결과 안내" : null,
                type == SmsMessageType.SMS ? 20 : 100,
                type,
                new BigDecimal(cost)
        );
    }
}
