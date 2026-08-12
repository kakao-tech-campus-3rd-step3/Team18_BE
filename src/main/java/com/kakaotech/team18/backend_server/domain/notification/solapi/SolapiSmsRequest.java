package com.kakaotech.team18.backend_server.domain.notification.solapi;

public record SolapiSmsRequest(
        String recipient,
        String text,
        String subject,
        String clientReference
) {
}
