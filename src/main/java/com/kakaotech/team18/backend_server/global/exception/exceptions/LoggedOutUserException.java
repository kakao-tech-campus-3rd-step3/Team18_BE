package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class LoggedOutUserException extends CustomException {
    public LoggedOutUserException() {
        super(ErrorCode.LOGGED_OUT_USER);
    }
}
