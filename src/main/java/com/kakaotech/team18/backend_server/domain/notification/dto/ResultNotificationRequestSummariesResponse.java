package com.kakaotech.team18.backend_server.domain.notification.dto;

import java.util.List;

public record ResultNotificationRequestSummariesResponse(
        List<ResultNotificationRequestSummary> requests
) {

    public ResultNotificationRequestSummariesResponse {
        requests = List.copyOf(requests);
    }
}
