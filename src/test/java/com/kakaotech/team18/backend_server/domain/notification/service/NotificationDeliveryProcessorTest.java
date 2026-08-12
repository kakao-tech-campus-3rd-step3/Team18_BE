package com.kakaotech.team18.backend_server.domain.notification.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.exception.NotificationSendException;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSenderRegistry;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryProcessorTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-08-12T03:00:00Z");
    private static final LocalDateTime ATTEMPTED_AT = LocalDateTime.ofInstant(NOW, SEOUL);

    @Mock
    private NotificationDeliveryStateService stateService;
    @Mock
    private NotificationSenderRegistry senderRegistry;
    @Mock
    private NotificationSender sender;

    private NotificationDeliveryProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new NotificationDeliveryProcessor(
                stateService,
                senderRegistry,
                Clock.fixed(NOW, SEOUL)
        );
        lenient().when(stateService.findChannel(1L))
                .thenReturn(Optional.of(NotificationChannel.EMAIL));
        lenient().when(senderRegistry.supports(NotificationChannel.EMAIL)).thenReturn(true);
    }

    @Test
    void completesClaimedDeliveryWhenSenderSucceeds() {
        NotificationMessage message = message();
        NotificationSendResult result = NotificationSendResult.sent("SMTP_SENT");
        when(stateService.claim(1L, ATTEMPTED_AT)).thenReturn(Optional.of(message));
        when(senderRegistry.get(NotificationChannel.EMAIL)).thenReturn(sender);
        when(sender.send(message)).thenReturn(result);

        processor.process(1L);

        verify(stateService).complete(1L, result, ATTEMPTED_AT);
    }

    @Test
    void reschedulesOnlyRetryableFailure() {
        NotificationMessage message = message();
        when(stateService.claim(1L, ATTEMPTED_AT)).thenReturn(Optional.of(message));
        when(senderRegistry.get(NotificationChannel.EMAIL)).thenReturn(sender);
        when(sender.send(message)).thenThrow(NotificationSendException.retryable(
                "EMAIL_TEMPORARY_FAILURE",
                "timeout",
                new RuntimeException()
        ));

        processor.process(1L);

        verify(stateService).reschedule(
                1L,
                ATTEMPTED_AT.plusMinutes(1),
                "EMAIL_TEMPORARY_FAILURE",
                "timeout"
        );
        verify(stateService, never()).failPermanently(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void usesExplicitRetryTimeForQuotaFailure() {
        NotificationMessage message = message();
        LocalDateTime nextHour = LocalDateTime.of(2026, 8, 12, 13, 0);
        when(stateService.claim(1L, ATTEMPTED_AT)).thenReturn(Optional.of(message));
        when(senderRegistry.get(NotificationChannel.EMAIL)).thenReturn(sender);
        when(sender.send(message)).thenThrow(NotificationSendException.retryableAt(
                "SOLAPI_HOURLY_QUOTA_EXCEEDED",
                "quota exceeded",
                nextHour,
                new RuntimeException()
        ));

        processor.process(1L);

        verify(stateService).reschedule(
                1L,
                nextHour,
                "SOLAPI_HOURLY_QUOTA_EXCEEDED",
                "quota exceeded"
        );
    }

    @Test
    void permanentlyFailsNonRetryableFailure() {
        NotificationMessage message = message();
        when(stateService.claim(1L, ATTEMPTED_AT)).thenReturn(Optional.of(message));
        when(senderRegistry.get(NotificationChannel.EMAIL)).thenReturn(sender);
        when(sender.send(message)).thenThrow(NotificationSendException.permanent(
                "EMAIL_AUTH_FAILED",
                "invalid credentials",
                new RuntimeException()
        ));

        processor.process(1L);

        verify(stateService).failPermanently(
                1L,
                ATTEMPTED_AT,
                "EMAIL_AUTH_FAILED",
                "invalid credentials"
        );
    }

    @Test
    void marksAmbiguousFailureUnknownWithoutRetrying() {
        NotificationMessage message = message();
        when(stateService.claim(1L, ATTEMPTED_AT)).thenReturn(Optional.of(message));
        when(senderRegistry.get(NotificationChannel.EMAIL)).thenReturn(sender);
        when(sender.send(message)).thenThrow(NotificationSendException.unknown(
                "EMAIL_TIMEOUT",
                "read timed out",
                new RuntimeException()
        ));

        processor.process(1L);

        verify(stateService).markUnknown(
                1L,
                ATTEMPTED_AT,
                "EMAIL_TIMEOUT",
                "read timed out"
        );
        verify(stateService, never()).reschedule(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void doesNothingWhenDeliveryCannotBeClaimed() {
        when(stateService.claim(1L, ATTEMPTED_AT)).thenReturn(Optional.empty());

        processor.process(1L);

        verify(senderRegistry, never()).get(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void leavesDeliveryPendingWhenChannelSenderIsDisabled() {
        when(senderRegistry.supports(NotificationChannel.EMAIL)).thenReturn(false);

        processor.process(1L);

        verify(stateService, never()).claim(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private NotificationMessage message() {
        return new NotificationMessage(
                1L,
                NotificationChannel.EMAIL,
                "applicant@example.com",
                "president@example.com",
                "결과 안내",
                "합격을 축하드립니다."
        );
    }
}
