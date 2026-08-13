package com.kakaotech.team18.backend_server.domain.notification.sms;

import java.math.BigDecimal;

public record PreparedSmsMessage(
        String recipient,
        String text,
        String subject,
        int byteLength,
        SmsMessageType type,
        BigDecimal estimatedCost
) {
}
