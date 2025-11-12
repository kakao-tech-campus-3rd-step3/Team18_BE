package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class NoApplicationException extends CustomException {
    public NoApplicationException(String message) {
        super(ErrorCode.NO_APPLICATION_PROCESSED);
    }
}
