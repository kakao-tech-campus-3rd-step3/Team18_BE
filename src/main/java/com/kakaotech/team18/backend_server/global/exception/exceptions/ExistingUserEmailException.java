package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ExistingUserEmailException extends CustomException {
    public ExistingUserEmailException(String message) {
        super(ErrorCode.EXISTING_USER_EMAIL, message);
    }
}
