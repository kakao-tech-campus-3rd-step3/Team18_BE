package com.kakaotech.team18.backend_server.domain.notification.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.exception.NotificationSendException;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiQuotaExceededException;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiSendQuota;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessagePolicy;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiClientException;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiSendResponse;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiSmsRequest;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiRecipientAllowlist;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmsNotificationSenderTest {

    @Mock
    private SolapiMessageClient messageClient;
    @Mock
    private SolapiSendQuota sendQuota;

    private SmsNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new SmsNotificationSender(messageClient, new SmsMessagePolicy(), sendQuota);
    }

    @Test
    void acceptsNormalizedSmsThroughSolapi() {
        SolapiSmsRequest request = new SolapiSmsRequest(
                "01012345678",
                "합격을 축하드립니다.",
                null,
                "10"
        );
        when(messageClient.send(request)).thenReturn(new SolapiSendResponse(
                "group-id",
                "message-id",
                "2000"
        ));

        NotificationSendResult result = sender.send(message("010-1234-5678", "합격을 축하드립니다."));

        verify(messageClient).send(request);
        verify(sendQuota).reserve(org.mockito.ArgumentMatchers.any());
        assertThat(result.outcome()).isEqualTo(NotificationSendResult.Outcome.ACCEPTED);
        assertThat(result.providerGroupId()).isEqualTo("group-id");
        assertThat(result.providerMessageId()).isEqualTo("message-id");
    }

    @Test
    void doesNotCallSolapiForInvalidRecipient() {
        assertThatThrownBy(() -> sender.send(message("02-123-4567", "결과 안내")))
                .isInstanceOfSatisfying(NotificationSendException.class, exception -> {
                    assertThat(exception.getDisposition())
                            .isEqualTo(NotificationSendException.FailureDisposition.PERMANENT);
                    assertThat(exception.getErrorCode()).isEqualTo("SMS_RECIPIENT_INVALID");
                });
    }

    @Test
    void blocksRecipientOutsideEnvironmentAllowlistBeforeQuotaAndApiCall() {
        sender = new SmsNotificationSender(
                messageClient,
                new SmsMessagePolicy(),
                sendQuota,
                new SolapiRecipientAllowlist(true, "01099998888")
        );

        assertThatThrownBy(() -> sender.send(message("01012345678", "결과 안내")))
                .isInstanceOfSatisfying(NotificationSendException.class, exception -> {
                    assertThat(exception.getDisposition())
                            .isEqualTo(NotificationSendException.FailureDisposition.PERMANENT);
                    assertThat(exception.getErrorCode()).isEqualTo("SMS_RECIPIENT_NOT_ALLOWED");
                });

        verify(sendQuota, never()).reserve(org.mockito.ArgumentMatchers.any());
        verify(messageClient, never()).send(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void mapsAmbiguousSolapiResultToUnknown() {
        SolapiSmsRequest request = new SolapiSmsRequest(
                "01012345678",
                "결과 안내",
                null,
                "10"
        );
        when(messageClient.send(request)).thenThrow(SolapiClientException.unknown(
                "SOLAPI_AMBIGUOUS_RESPONSE",
                "response unknown",
                null
        ));

        assertThatThrownBy(() -> sender.send(message("01012345678", "결과 안내")))
                .isInstanceOfSatisfying(NotificationSendException.class, exception ->
                        assertThat(exception.getDisposition())
                                .isEqualTo(NotificationSendException.FailureDisposition.UNKNOWN));
    }

    @Test
    void reschedulesAtNextHourWithoutCallingSolapiWhenQuotaIsFull() {
        java.time.LocalDateTime retryAt = java.time.LocalDateTime.of(2026, 8, 12, 14, 0);
        org.mockito.Mockito.doThrow(new SolapiQuotaExceededException(
                        "SOLAPI_HOURLY_QUOTA_EXCEEDED",
                        "hourly quota exceeded",
                        retryAt
                ))
                .when(sendQuota).reserve(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> sender.send(message("01012345678", "결과 안내")))
                .isInstanceOfSatisfying(NotificationSendException.class, exception -> {
                    assertThat(exception.getDisposition())
                            .isEqualTo(NotificationSendException.FailureDisposition.RETRYABLE);
                    assertThat(exception.getRetryAt()).isEqualTo(retryAt);
                    assertThat(exception.getErrorCode()).isEqualTo("SOLAPI_HOURLY_QUOTA_EXCEEDED");
                });
        verify(messageClient, never()).send(org.mockito.ArgumentMatchers.any());
    }

    private NotificationMessage message(String recipient, String body) {
        return new NotificationMessage(
                10L,
                NotificationChannel.SMS,
                recipient,
                null,
                null,
                body,
                1
        );
    }
}
