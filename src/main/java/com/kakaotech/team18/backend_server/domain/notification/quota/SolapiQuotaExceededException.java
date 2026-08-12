package com.kakaotech.team18.backend_server.domain.notification.quota;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SolapiQuotaExceededException extends RuntimeException {

    private final LocalDateTime retryAt;

    public SolapiQuotaExceededException(LocalDateTime retryAt) {
        super("시간당 SOLAPI 호출 한도에 도달했습니다.");
        this.retryAt = retryAt;
    }
}
