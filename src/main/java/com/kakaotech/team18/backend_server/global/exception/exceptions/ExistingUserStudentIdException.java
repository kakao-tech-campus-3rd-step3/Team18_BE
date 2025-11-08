package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ExistingUserStudentIdException extends CustomException {
    public ExistingUserStudentIdException(String message) {
        super(ErrorCode.EXISTING_USER_STUDENT_ID, message);
    }
}
