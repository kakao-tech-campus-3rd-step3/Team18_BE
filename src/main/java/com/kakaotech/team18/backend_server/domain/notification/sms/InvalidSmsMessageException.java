package com.kakaotech.team18.backend_server.domain.notification.sms;

import lombok.Getter;

@Getter
public class InvalidSmsMessageException extends RuntimeException {

    private final String errorCode;

    public InvalidSmsMessageException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
