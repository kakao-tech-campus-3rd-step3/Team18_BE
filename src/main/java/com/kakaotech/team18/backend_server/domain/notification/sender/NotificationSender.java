package com.kakaotech.team18.backend_server.domain.notification.sender;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;

public interface NotificationSender {

    NotificationChannel channel();

    NotificationSendResult send(NotificationMessage message);
}
