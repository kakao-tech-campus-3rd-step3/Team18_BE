package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class UnscheduledAcceptedApplicantExistsException extends CustomException {
    public UnscheduledAcceptedApplicantExistsException() {
        super(ErrorCode.UNSCHEDULED_ACCEPTED_APPLICANT_EXISTS);
    }
}
