package com.kakaotech.team18.backend_server.domain.notification.solapi;

public record SolapiSendResponse(
        String groupId,
        String messageId,
        String statusCode
) {
}
