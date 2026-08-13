package com.kakaotech.team18.backend_server.domain.notification.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationRequestStatus;
import java.time.LocalDateTime;

public record ResultNotificationRequestSummary(
        Long requestId,
        String idempotencyKey,
        Stage stage,
        NotificationRequestStatus requestStatus,
        LocalDateTime requestedAt,
        Long total,
        Long pending,
        Long accepted,
        Long sent,
        Long failed,
        Long unknown
) {
}
