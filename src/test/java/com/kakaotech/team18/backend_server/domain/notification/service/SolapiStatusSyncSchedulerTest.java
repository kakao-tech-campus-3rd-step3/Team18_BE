package com.kakaotech.team18.backend_server.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService.AcceptedDelivery;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiClientException;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageStatus;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiStatusResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SolapiStatusSyncSchedulerTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant NOW_INSTANT = Instant.parse("2026-08-12T04:30:00Z");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 12, 13, 30);

    @Mock
    private NotificationDeliveryRepository repository;
    @Mock
    private NotificationDeliveryStateService stateService;
    @Mock
    private SolapiMessageClient messageClient;

    private SolapiStatusSyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SolapiStatusSyncScheduler(
                repository,
                stateService,
                messageClient,
                50,
                60,
                24,
                Clock.fixed(NOW_INSTANT, SEOUL)
        );
    }

    @Test
    void appliesFinalProviderStatus() {
        when(repository.findDueAcceptedDeliveryIds(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(1L));
        when(stateService.findAccepted(1L)).thenReturn(Optional.of(new AcceptedDelivery(
                1L,
                "message-id",
                NOW.minusMinutes(10)
        )));
        SolapiStatusResponse response = new SolapiStatusResponse(
                SolapiMessageStatus.SENT,
                "2000"
        );
        when(messageClient.getStatus("message-id")).thenReturn(response);

        scheduler.synchronizeStatuses();

        verify(stateService).applyProviderStatus(
                1L,
                response,
                NOW,
                NOW.plusMinutes(1)
        );
    }

    @Test
    void defersStatusCheckWhenLookupFails() {
        when(repository.findDueAcceptedDeliveryIds(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(1L));
        when(stateService.findAccepted(1L)).thenReturn(Optional.of(new AcceptedDelivery(
                1L,
                "message-id",
                NOW.minusMinutes(10)
        )));
        when(messageClient.getStatus("message-id")).thenThrow(SolapiClientException.unknown(
                "SOLAPI_AMBIGUOUS_RESPONSE",
                "lookup failed",
                null
        ));

        scheduler.synchronizeStatuses();

        verify(stateService).recordStatusCheckFailure(
                1L,
                NOW.plusMinutes(1),
                "SOLAPI_AMBIGUOUS_RESPONSE",
                "lookup failed"
        );
    }

    @Test
    void marksAcceptedMessageUnknownAfterMaximumAge() {
        when(repository.findDueAcceptedDeliveryIds(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(1L));
        when(stateService.findAccepted(1L)).thenReturn(Optional.of(new AcceptedDelivery(
                1L,
                "message-id",
                NOW.minusHours(24)
        )));

        scheduler.synchronizeStatuses();

        verify(stateService).markAcceptedUnknown(1L, NOW);
        verify(messageClient, never()).getStatus(any());
    }
}
