package com.kakaotech.team18.backend_server.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchSchedulerTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant NOW_INSTANT = Instant.parse("2026-08-12T04:30:00Z");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 12, 13, 30);
    private static final LocalDateTime CUTOFF = LocalDateTime.of(2026, 8, 12, 13, 20);

    @Mock
    private NotificationDeliveryRepository repository;
    @Mock
    private NotificationDeliveryProcessor processor;
    @Mock
    private NotificationDeliveryStateService stateService;

    private NotificationDispatchScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationDispatchScheduler(
                repository,
                processor,
                stateService,
                50,
                600,
                Clock.fixed(NOW_INSTANT, SEOUL)
        );
    }

    @Test
    void processesDueDeliveriesAndIsolatesEachFailure() {
        when(repository.findDueDeliveryIds(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(1L, 2L));
        when(repository.findStaleSendingDeliveryIds(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());
        doThrow(new RuntimeException("first failed")).when(processor).process(1L);

        scheduler.dispatchAndRecover();

        verify(repository).findDueDeliveryIds(any(LocalDateTime.class), any(Pageable.class));
        verify(processor).process(1L);
        verify(processor).process(2L);
    }

    @Test
    void marksTimedOutSendingDeliveriesUnknown() {
        when(repository.findDueDeliveryIds(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());
        when(repository.findStaleSendingDeliveryIds(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(3L));

        scheduler.dispatchAndRecover();

        verify(repository).findStaleSendingDeliveryIds(any(LocalDateTime.class), any(Pageable.class));
        verify(stateService).markStaleSendingUnknown(3L, CUTOFF, NOW);
    }
}
