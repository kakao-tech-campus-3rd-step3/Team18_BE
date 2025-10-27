package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class UnRegisteredUserException extends CustomException {

    public UnRegisteredUserException(String detail) {
        super(ErrorCode.UNREGISTERED_USER, detail);
    }
}
