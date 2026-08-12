package com.kakaotech.team18.backend_server.domain.notification.sms;

public record PreparedSmsMessage(
        String recipient,
        String text,
        String subject,
        int byteLength,
        SmsMessageType type
) {
}
