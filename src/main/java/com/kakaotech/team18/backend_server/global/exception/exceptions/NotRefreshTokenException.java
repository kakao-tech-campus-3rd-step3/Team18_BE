package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class NotRefreshTokenException extends CustomException {
    public NotRefreshTokenException() {
        super(ErrorCode.NOT_REFRESH_TOKEN);
    }
}
