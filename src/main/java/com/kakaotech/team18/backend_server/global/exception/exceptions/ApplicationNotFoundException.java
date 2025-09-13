package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ApplicationNotFoundException extends CustomException {

    public ApplicationNotFoundException(String detail) {
        super(ErrorCode.APPLICATION_NOT_FOUND, detail);
    }
}
