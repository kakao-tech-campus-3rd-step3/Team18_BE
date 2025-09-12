package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ClubNotFoundException extends CustomException {

    public ClubNotFoundException(String detail) {
        super(ErrorCode.CLUB_NOT_FOUND, detail);
    }
}
