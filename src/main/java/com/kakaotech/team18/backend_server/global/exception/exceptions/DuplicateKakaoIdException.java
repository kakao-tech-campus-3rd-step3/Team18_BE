package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class DuplicateKakaoIdException extends CustomException {
    public DuplicateKakaoIdException(String message) {
        super(ErrorCode.DUPLICATE_KAKAO_ID, message);
    }
}
