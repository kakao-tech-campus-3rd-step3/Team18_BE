package com.kakaotech.team18.backend_server.domain.notification.solapi;

import lombok.Getter;

@Getter
public class SolapiClientException extends RuntimeException {

    public enum FailureType {
        RETRYABLE,
        PERMANENT,
        UNKNOWN
    }

    private final String errorCode;
    private final FailureType failureType;

    private SolapiClientException(
            String errorCode,
            String message,
            FailureType failureType,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.failureType = failureType;
    }

    public static SolapiClientException permanent(String errorCode, String message, Throwable cause) {
        return new SolapiClientException(errorCode, message, FailureType.PERMANENT, cause);
    }

    public static SolapiClientException retryable(String errorCode, String message, Throwable cause) {
        return new SolapiClientException(errorCode, message, FailureType.RETRYABLE, cause);
    }

    public static SolapiClientException unknown(String errorCode, String message, Throwable cause) {
        return new SolapiClientException(errorCode, message, FailureType.UNKNOWN, cause);
    }
}
