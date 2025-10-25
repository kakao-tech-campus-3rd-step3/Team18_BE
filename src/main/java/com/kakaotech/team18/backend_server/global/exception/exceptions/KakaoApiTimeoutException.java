package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

/**
 * 카카오 API 호출 시 타임아웃이 발생했을 때 던져지는 예외입니다.
 */
public class KakaoApiTimeoutException extends CustomException {
    public KakaoApiTimeoutException() {
        super(ErrorCode.KAKAO_API_TIMEOUT);
    }
}
