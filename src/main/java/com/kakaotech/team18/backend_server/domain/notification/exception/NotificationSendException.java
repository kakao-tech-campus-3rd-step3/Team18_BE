package com.kakaotech.team18.backend_server.domain.notification.exception;

import lombok.Getter;

@Getter
public class NotificationSendException extends RuntimeException {

    private final String errorCode;
    private final FailureDisposition disposition;

    public enum FailureDisposition {
        RETRYABLE,
        UNKNOWN,
        PERMANENT
    }

    private NotificationSendException(
            String errorCode,
            String message,
            FailureDisposition disposition,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.disposition = disposition;
    }

    public static NotificationSendException retryable(String errorCode, String message, Throwable cause) {
        return new NotificationSendException(
                errorCode,
                message,
                FailureDisposition.RETRYABLE,
                cause
        );
    }

    public static NotificationSendException unknown(String errorCode, String message, Throwable cause) {
        return new NotificationSendException(
                errorCode,
                message,
                FailureDisposition.UNKNOWN,
                cause
        );
    }

    public static NotificationSendException permanent(String errorCode, String message, Throwable cause) {
        return new NotificationSendException(
                errorCode,
                message,
                FailureDisposition.PERMANENT,
                cause
        );
    }
}
