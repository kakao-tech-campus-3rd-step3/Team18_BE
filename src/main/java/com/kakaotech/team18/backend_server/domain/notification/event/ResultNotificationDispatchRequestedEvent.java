package com.kakaotech.team18.backend_server.domain.notification.event;

import java.util.List;

public record ResultNotificationDispatchRequestedEvent(List<Long> deliveryIds) {
    public ResultNotificationDispatchRequestedEvent {
        deliveryIds = List.copyOf(deliveryIds);
    }
}
