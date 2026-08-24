package com.kakaotech.team18.backend_server.domain.notification.solapi;

public record SolapiSmsRequest(
        String recipient,
        String text,
        String subject,
        String clientReference,
        String idempotencyKey
) {

    public SolapiSmsRequest(
            String recipient,
            String text,
            String subject,
            String clientReference
    ) {
        this(recipient, text, subject, clientReference, null);
    }
}
