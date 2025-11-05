package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class KakaoApiException extends CustomException {

    public KakaoApiException() {
        super(ErrorCode.KAKAO_API_ERROR);
    }
}
