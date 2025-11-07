package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidJwtException extends CustomException {

    public InvalidJwtException(ErrorCode errorCode) {
        super(errorCode, null);
    }

    public InvalidJwtException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
