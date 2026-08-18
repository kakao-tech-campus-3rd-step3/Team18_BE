package com.kakaotech.team18.backend_server.domain.notification.solapi;

public record SolapiAccountQuotaResponse(
        int dailyQuota,
        boolean autoAdjustment
) {

    public SolapiAccountQuotaResponse {
        if (dailyQuota <= 0) {
            throw new IllegalArgumentException("SOLAPI 일일 발송 한도는 0보다 커야 합니다.");
        }
    }
}
