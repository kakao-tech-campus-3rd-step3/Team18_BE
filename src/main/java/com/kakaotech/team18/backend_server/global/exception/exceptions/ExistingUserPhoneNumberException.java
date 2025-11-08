package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ExistingUserPhoneNumberException extends CustomException {
    public ExistingUserPhoneNumberException(String message) {
        super(ErrorCode.EXISTING_USER_PHONE_NUMBER, message);
    }
}
