package com.kakaotech.team18.backend_server.domain.notification.webhook;

import java.util.List;

public record SolapiWebhookReceivedEvent(List<SolapiWebhookReport> reports) {

    public SolapiWebhookReceivedEvent {
        reports = List.copyOf(reports);
    }
}
