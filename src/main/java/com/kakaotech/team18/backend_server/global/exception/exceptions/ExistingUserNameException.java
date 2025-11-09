package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ExistingUserNameException extends CustomException {
    public ExistingUserNameException(String message) {
        super(ErrorCode.EXISTING_USER_NAME, message);
    }
}
