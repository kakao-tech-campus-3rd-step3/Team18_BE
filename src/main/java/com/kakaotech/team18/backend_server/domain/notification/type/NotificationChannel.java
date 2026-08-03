package com.kakaotech.team18.backend_server.domain.notification.type;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지원 결과 알림 전송 채널")
public enum NotificationChannel {
    EMAIL,
    SMS
}
