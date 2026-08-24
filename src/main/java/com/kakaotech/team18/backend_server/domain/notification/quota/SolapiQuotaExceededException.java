package com.kakaotech.team18.backend_server.domain.notification.quota;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SolapiQuotaExceededException extends RuntimeException {

    private final LocalDateTime retryAt;
    private final String errorCode;

    public SolapiQuotaExceededException(String errorCode, String message, LocalDateTime retryAt) {
        super(message);
        this.errorCode = errorCode;
        this.retryAt = retryAt;
    }
}
