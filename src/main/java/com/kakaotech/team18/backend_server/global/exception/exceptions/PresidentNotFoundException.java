package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class PresidentNotFoundException extends CustomException {
    public PresidentNotFoundException(String detail) {
        super(ErrorCode.PRESIDENT_NOT_FOUND, detail);
    }
}
