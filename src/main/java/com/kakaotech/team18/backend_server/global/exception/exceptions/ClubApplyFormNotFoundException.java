package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ClubApplyFormNotFoundException extends CustomException {

    public ClubApplyFormNotFoundException(String detail) {
        super(ErrorCode.FORM_NOT_FOUND, detail);
    }
}
