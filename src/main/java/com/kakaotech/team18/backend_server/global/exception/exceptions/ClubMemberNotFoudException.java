package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ClubMemberNotFoudException extends CustomException {

    public ClubMemberNotFoudException(String detail) {
        super(ErrorCode.CLUB_MEMBER_NOT_FOUND, detail);
    }
}
