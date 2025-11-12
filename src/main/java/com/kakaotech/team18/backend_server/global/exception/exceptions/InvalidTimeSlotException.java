package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidTimeSlotException extends CustomException {
    public InvalidTimeSlotException(String message) {
        super(ErrorCode.INVALID_TIME_SLOT, message);
    }
}
