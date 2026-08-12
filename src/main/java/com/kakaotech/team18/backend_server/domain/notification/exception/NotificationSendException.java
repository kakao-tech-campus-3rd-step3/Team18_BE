package com.kakaotech.team18.backend_server.domain.notification.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class NotificationSendException extends RuntimeException {

    private final String errorCode;
    private final FailureDisposition disposition;
    private final LocalDateTime retryAt;

    public enum FailureDisposition {
        RETRYABLE,
        UNKNOWN,
        PERMANENT
    }

    private NotificationSendException(
            String errorCode,
            String message,
            FailureDisposition disposition,
            LocalDateTime retryAt,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.disposition = disposition;
        this.retryAt = retryAt;
    }

    public static NotificationSendException retryable(String errorCode, String message, Throwable cause) {
        return new NotificationSendException(
                errorCode,
                message,
                FailureDisposition.RETRYABLE,
                null,
                cause
        );
    }

    public static NotificationSendException retryableAt(
            String errorCode,
            String message,
            LocalDateTime retryAt,
            Throwable cause
    ) {
        return new NotificationSendException(
                errorCode,
                message,
                FailureDisposition.RETRYABLE,
                retryAt,
                cause
        );
    }

    public static NotificationSendException unknown(String errorCode, String message, Throwable cause) {
        return new NotificationSendException(
                errorCode,
                message,
                FailureDisposition.UNKNOWN,
                null,
                cause
        );
    }

    public static NotificationSendException permanent(String errorCode, String message, Throwable cause) {
        return new NotificationSendException(
                errorCode,
                message,
                FailureDisposition.PERMANENT,
                null,
                cause
        );
    }
}
