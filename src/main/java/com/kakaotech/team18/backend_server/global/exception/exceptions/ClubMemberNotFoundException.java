package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ClubMemberNotFoundException extends CustomException {

    public ClubMemberNotFoundException(String detail) {
        super(ErrorCode.CLUB_MEMBER_NOT_FOUND, detail);
    }
}
