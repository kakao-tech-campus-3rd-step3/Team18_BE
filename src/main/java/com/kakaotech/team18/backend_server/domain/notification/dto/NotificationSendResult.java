package com.kakaotech.team18.backend_server.domain.notification.dto;

import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessageType;
import java.math.BigDecimal;

public record NotificationSendResult(
        Outcome outcome,
        String providerGroupId,
        String providerMessageId,
        String providerStatusCode,
        SmsMessageType messageType,
        BigDecimal estimatedCost
) {
    public enum Outcome {
        ACCEPTED,
        SENT
    }

    public static NotificationSendResult sent(String providerStatusCode) {
        return new NotificationSendResult(
                Outcome.SENT,
                null,
                null,
                providerStatusCode,
                null,
                BigDecimal.ZERO
        );
    }

    public static NotificationSendResult accepted(
            String providerGroupId,
            String providerMessageId,
            String providerStatusCode
    ) {
        return new NotificationSendResult(
                Outcome.ACCEPTED,
                providerGroupId,
                providerMessageId,
                providerStatusCode,
                null,
                BigDecimal.ZERO
        );
    }

    public static NotificationSendResult accepted(
            String providerGroupId,
            String providerMessageId,
            String providerStatusCode,
            SmsMessageType messageType,
            BigDecimal estimatedCost
    ) {
        return new NotificationSendResult(
                Outcome.ACCEPTED,
                providerGroupId,
                providerMessageId,
                providerStatusCode,
                messageType,
                estimatedCost
        );
    }
}
