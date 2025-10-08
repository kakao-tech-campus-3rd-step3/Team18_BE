package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class TemporaryServerConflictException extends CustomException {

    /**
     * 데이터베이스 락 경합 등 일시적인 서버 충돌 시 발생하는 예외입니다.
     * @param detail 디버깅 및 로깅을 위한 상세 정보 (e.g., 원인 예외 메시지)
     */
    public TemporaryServerConflictException(String detail) {
        super(ErrorCode.TEMPORARY_SERVER_CONFLICT, detail);
    }
}
