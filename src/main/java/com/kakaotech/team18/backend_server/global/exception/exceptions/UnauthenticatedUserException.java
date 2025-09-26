package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class UnauthenticatedUserException extends CustomException {
    public UnauthenticatedUserException() {
        super(ErrorCode.UNAUTHENTICATED_USER);
    }
}
