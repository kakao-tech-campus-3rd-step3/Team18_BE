package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class CannotDeleteSelfException extends CustomException {
    public CannotDeleteSelfException() {
        super(ErrorCode.CANNOT_DELETE_SELF);
    }
}
