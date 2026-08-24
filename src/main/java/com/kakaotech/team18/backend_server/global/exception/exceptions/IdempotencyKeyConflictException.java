package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class IdempotencyKeyConflictException extends CustomException {

    public IdempotencyKeyConflictException() {
        super(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
    }
}
