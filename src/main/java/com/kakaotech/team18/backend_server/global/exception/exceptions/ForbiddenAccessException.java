package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ForbiddenAccessException extends CustomException {
    public ForbiddenAccessException() {
        super(ErrorCode.FORBIDDEN);
    }
}
