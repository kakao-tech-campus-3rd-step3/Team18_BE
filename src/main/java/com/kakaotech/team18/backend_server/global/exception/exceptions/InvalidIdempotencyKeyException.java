package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidIdempotencyKeyException extends CustomException {

    public InvalidIdempotencyKeyException() {
        super(ErrorCode.INVALID_INPUT_VALUE, "Idempotency-Key는 1자 이상 100자 이하여야 합니다.");
    }
}
