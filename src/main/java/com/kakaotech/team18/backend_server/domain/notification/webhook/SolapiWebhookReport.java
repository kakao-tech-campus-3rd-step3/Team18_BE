package com.kakaotech.team18.backend_server.domain.notification.webhook;

import java.time.OffsetDateTime;
import java.util.Map;

public record SolapiWebhookReport(
        String messageId,
        String groupId,
        String type,
        String statusCode,
        String statusMessage,
        OffsetDateTime dateProcessed,
        Map<String, Object> customFields
) {
}
