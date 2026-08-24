package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationRetentionSchedulerTest {

    private final NotificationDeliveryRepository repository = mock(NotificationDeliveryRepository.class);
    private final NotificationDeliveryStateService stateService = mock(NotificationDeliveryStateService.class);
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-08-17T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    void redactsOnlyExpiredTerminalDeliveriesInConfiguredBatch() {
        given(repository.findRetentionTargetIds(any(), any(), any(Pageable.class)))
                .willReturn(List.of(10L, 11L));
        given(stateService.redactSensitiveData(eq(10L), any())).willReturn(true);
        given(stateService.redactSensitiveData(eq(11L), any())).willReturn(false);
        NotificationRetentionScheduler scheduler = new NotificationRetentionScheduler(
                repository,
                stateService,
                90,
                50,
                clock
        );

        scheduler.redactExpiredSensitiveData();

        ArgumentCaptor<List<NotificationDeliveryStatus>> statuses = ArgumentCaptor.forClass(List.class);
        verify(repository).findRetentionTargetIds(
                statuses.capture(),
                eq(LocalDateTime.of(2026, 5, 19, 12, 0)),
                any(Pageable.class)
        );
        assertThat(statuses.getValue()).containsExactly(
                NotificationDeliveryStatus.SENT,
                NotificationDeliveryStatus.FAILED,
                NotificationDeliveryStatus.UNKNOWN,
                NotificationDeliveryStatus.PERMANENTLY_FAILED
        );
        verify(stateService).redactSensitiveData(
                10L,
                LocalDateTime.of(2026, 8, 17, 12, 0)
        );
    }

    @Test
    void rejectsInvalidRetentionSettings() {
        assertThatThrownBy(() -> new NotificationRetentionScheduler(
                repository,
                stateService,
                0,
                100,
                clock
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
