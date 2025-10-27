package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class NoticeNotFoundException extends CustomException {
    public NoticeNotFoundException(String message) {
        super(ErrorCode.NOTICE_NOT_FOUND, message);
    }
}
