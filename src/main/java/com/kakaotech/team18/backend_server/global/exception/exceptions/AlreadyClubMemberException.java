package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class AlreadyClubMemberException extends CustomException {
    public AlreadyClubMemberException(String detail) {
        super(ErrorCode.ALREADY_CLUB_MEMBER, detail);
    }
}
