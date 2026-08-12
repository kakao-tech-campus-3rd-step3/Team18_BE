package com.kakaotech.team18.backend_server.domain.notification.quota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationQuotaBucket;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationQuotaBucketRepository;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
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

@ExtendWith(MockitoExtension.class)
class SolapiHourlyQuotaServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-08-12T04:30:00Z");
    private static final LocalDateTime BUCKET_START = LocalDateTime.of(2026, 8, 12, 13, 0);

    @Mock
    private NotificationQuotaBucketRepository repository;

    private SolapiHourlyQuotaService service;

    @BeforeEach
    void setUp() {
        service = new SolapiHourlyQuotaService(repository, 2, Clock.fixed(NOW, SEOUL));
    }

    @Test
    void createsCurrentHourBucketAndReservesOneCall() {
        when(repository.findByChannelAndBucketStartedAt(NotificationChannel.SMS, BUCKET_START))
                .thenReturn(Optional.empty());

        service.reserve();

        ArgumentCaptor<NotificationQuotaBucket> captor =
                ArgumentCaptor.forClass(NotificationQuotaBucket.class);
        verify(repository).saveAndFlush(captor.capture());
        NotificationQuotaBucket saved = captor.getValue();
        assertThat(saved.getBucketStartedAt()).isEqualTo(BUCKET_START);
        assertThat(saved.getRequestCount()).isEqualTo(1);
    }

    @Test
    void rejectsCallAtLimitAndReturnsNextHour() {
        NotificationQuotaBucket bucket = NotificationQuotaBucket.hourly(
                NotificationChannel.SMS,
                BUCKET_START
        );
        bucket.tryReserve(2);
        bucket.tryReserve(2);
        when(repository.findByChannelAndBucketStartedAt(NotificationChannel.SMS, BUCKET_START))
                .thenReturn(Optional.of(bucket));

        assertThatThrownBy(service::reserve)
                .isInstanceOfSatisfying(SolapiQuotaExceededException.class, exception ->
                        assertThat(exception.getRetryAt())
                                .isEqualTo(LocalDateTime.of(2026, 8, 12, 14, 0)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThatThrownBy(() -> new SolapiHourlyQuotaService(
                repository,
                0,
                Clock.fixed(NOW, SEOUL)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
