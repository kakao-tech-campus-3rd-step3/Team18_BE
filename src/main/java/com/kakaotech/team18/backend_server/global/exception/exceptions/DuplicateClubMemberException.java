package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class DuplicateClubMemberException extends CustomException {

    public DuplicateClubMemberException(String detail) {
        super(ErrorCode.DUPLICATE_CLUB_MEMBER, detail);
    }
}