package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class PendingApplicationsExistException extends CustomException {
    public PendingApplicationsExistException() {
        super(ErrorCode.PENDING_APPLICATION_EXIST);
    }
}
