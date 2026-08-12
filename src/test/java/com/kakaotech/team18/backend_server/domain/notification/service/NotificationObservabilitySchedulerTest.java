package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService.FailureAlert;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationObservabilitySchedulerTest {

    private final NotificationDeliveryRepository repository = mock(NotificationDeliveryRepository.class);
    private final NotificationDeliveryStateService stateService = mock(NotificationDeliveryStateService.class);
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-08-12T04:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("상태별 건수를 메트릭에 반영하고 새 실패를 한 번 보고 처리한다")
    void refreshMetricsAndClaimFailures() {
        given(repository.countByStatus(any())).willAnswer(invocation ->
                invocation.getArgument(0) == NotificationDeliveryStatus.PENDING ? 3L : 0L
        );
        given(repository.findUnalertedFailureIds(any(), any(Pageable.class)))
                .willReturn(List.of(11L));
        given(stateService.claimFailureAlert(eq(11L), any())).willReturn(Optional.of(
                new FailureAlert(
                        11L,
                        NotificationChannel.SMS,
                        NotificationDeliveryStatus.PERMANENTLY_FAILED,
                        "INVALID_RECIPIENT"
                )
        ));
        NotificationObservabilityScheduler scheduler = new NotificationObservabilityScheduler(
                repository,
                stateService,
                meterRegistry,
                10,
                clock
        );

        scheduler.monitor();

        assertThat(meterRegistry.get("notification.delivery.status")
                .tag("status", "PENDING")
                .gauge()
                .value()).isEqualTo(3.0);
        verify(stateService).claimFailureAlert(eq(11L), any());
        ArgumentCaptor<List<NotificationDeliveryStatus>> statuses = ArgumentCaptor.forClass(List.class);
        verify(repository).findUnalertedFailureIds(statuses.capture(), any(Pageable.class));
        assertThat(statuses.getValue()).containsExactly(
                NotificationDeliveryStatus.FAILED,
                NotificationDeliveryStatus.UNKNOWN,
                NotificationDeliveryStatus.PERMANENTLY_FAILED
        );
    }

    @Test
    @DisplayName("실패 알림 배치 크기는 양수여야 한다")
    void rejectInvalidBatchSize() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                new NotificationObservabilityScheduler(
                        repository,
                        stateService,
                        meterRegistry,
                        0,
                        clock
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
