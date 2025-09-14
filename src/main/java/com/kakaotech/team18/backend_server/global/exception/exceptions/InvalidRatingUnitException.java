package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidRatingUnitException extends CustomException {

    public InvalidRatingUnitException() {
        super(ErrorCode.INVALID_RATING_UNIT);
    }
}
