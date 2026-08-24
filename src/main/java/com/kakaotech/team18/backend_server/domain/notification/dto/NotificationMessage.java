package com.kakaotech.team18.backend_server.domain.notification.dto;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;

public record NotificationMessage(
        Long deliveryId,
        String idempotencyKey,
        NotificationChannel channel,
        String recipientAddress,
        String replyToAddress,
        String subject,
        String body,
        int attemptCount
) {
    public static NotificationMessage from(NotificationDelivery delivery) {
        return new NotificationMessage(
                delivery.getId(),
                delivery.getIdempotencyKey(),
                delivery.getChannel(),
                delivery.getRecipientAddress(),
                delivery.getReplyToAddress(),
                delivery.getMessageSubject(),
                delivery.getMessageBody(),
                delivery.getAttemptCount()
        );
    }

    public NotificationMessage(
            Long deliveryId,
            NotificationChannel channel,
            String recipientAddress,
            String replyToAddress,
            String subject,
            String body,
            int attemptCount
    ) {
        this(
                deliveryId,
                null,
                channel,
                recipientAddress,
                replyToAddress,
                subject,
                body,
                attemptCount
        );
    }
}
