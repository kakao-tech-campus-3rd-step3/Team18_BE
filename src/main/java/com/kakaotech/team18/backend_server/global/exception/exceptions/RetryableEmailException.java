package com.kakaotech.team18.backend_server.global.exception.exceptions;

//이 에러는 @Retry 에서 사용하는거라 에러코드가 없습니다. 프론트에 넘겨주지 않아요
public class RetryableEmailException extends RuntimeException {
    public RetryableEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
