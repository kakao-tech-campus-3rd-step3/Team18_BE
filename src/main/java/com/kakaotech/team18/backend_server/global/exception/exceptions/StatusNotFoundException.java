package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class StatusNotFoundException extends CustomException {

    public StatusNotFoundException(String detail) {
        super(ErrorCode.STATUS_NOT_FOUND, detail);
    }
}
