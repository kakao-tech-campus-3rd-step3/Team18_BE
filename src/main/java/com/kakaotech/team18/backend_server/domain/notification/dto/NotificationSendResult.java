package com.kakaotech.team18.backend_server.domain.notification.dto;

public record NotificationSendResult(
        Outcome outcome,
        String providerGroupId,
        String providerMessageId,
        String providerStatusCode
) {
    public enum Outcome {
        ACCEPTED,
        SENT
    }

    public static NotificationSendResult sent(String providerStatusCode) {
        return new NotificationSendResult(Outcome.SENT, null, null, providerStatusCode);
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
                providerStatusCode
        );
    }
}
