package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidAnswerException extends CustomException {
    public InvalidAnswerException(String detail) {
        super(ErrorCode.INVALID_INPUT_ANSWER,detail);
    }
}
