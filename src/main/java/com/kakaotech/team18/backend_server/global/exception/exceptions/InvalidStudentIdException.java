package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidStudentIdException extends CustomException {

    public InvalidStudentIdException(String detail) {
        super(ErrorCode.INVALID_STUDENT_ID, detail);
    }
}