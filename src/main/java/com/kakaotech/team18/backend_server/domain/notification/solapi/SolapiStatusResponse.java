package com.kakaotech.team18.backend_server.domain.notification.solapi;

public record SolapiStatusResponse(
        SolapiMessageStatus status,
        String providerStatusCode
) {
}
