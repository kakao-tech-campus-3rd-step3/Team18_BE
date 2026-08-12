package com.kakaotech.team18.backend_server.domain.notification.dto;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;

public record NotificationMessage(
        Long deliveryId,
        NotificationChannel channel,
        String recipientAddress,
        String replyToAddress,
        String subject,
        String body
) {
    public static NotificationMessage from(NotificationDelivery delivery) {
        return new NotificationMessage(
                delivery.getId(),
                delivery.getChannel(),
                delivery.getRecipientAddress(),
                delivery.getReplyToAddress(),
                delivery.getMessageSubject(),
                delivery.getMessageBody()
        );
    }
}
