package com.kakaotech.team18.backend_server.domain.notification.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationRetryPolicy {

    private static final String QUOTA_ERROR_SUFFIX = "_QUOTA_EXCEEDED";

    private final int maxAttempts;
    private final long initialDelaySeconds;
    private final long maxDelaySeconds;

    public NotificationRetryPolicy(
            @Value("${notification.dispatch.max-attempts:5}") int maxAttempts,
            @Value("${notification.dispatch.initial-retry-delay-seconds:60}") long initialDelaySeconds,
            @Value("${notification.dispatch.max-retry-delay-seconds:3600}") long maxDelaySeconds
    ) {
        if (maxAttempts <= 0 || initialDelaySeconds <= 0 || maxDelaySeconds < initialDelaySeconds) {
            throw new IllegalArgumentException("알림 재시도 설정값이 올바르지 않습니다.");
        }
        this.maxAttempts = maxAttempts;
        this.initialDelaySeconds = initialDelaySeconds;
        this.maxDelaySeconds = maxDelaySeconds;
    }

    public boolean exhausted(int attemptCount, String errorCode) {
        return (errorCode == null || !errorCode.endsWith(QUOTA_ERROR_SUFFIX))
                && attemptCount >= maxAttempts;
    }

    public LocalDateTime nextRetryAt(LocalDateTime failedAt, int attemptCount) {
        long delay = initialDelaySeconds;
        for (int i = 1; i < attemptCount && delay < maxDelaySeconds; i++) {
            delay = Math.min(delay * 2, maxDelaySeconds);
        }
        return failedAt.plusSeconds(delay);
    }
}
