package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ExpiredRefreshTokenException extends CustomException {

    public ExpiredRefreshTokenException() {
        super(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }
}
