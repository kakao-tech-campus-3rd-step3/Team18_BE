package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class DuplicateClubAdminException extends CustomException {

    public DuplicateClubAdminException(String detail) {
        super(ErrorCode.DUPLICATE_CLUB_ADMIN, detail);
    }
}