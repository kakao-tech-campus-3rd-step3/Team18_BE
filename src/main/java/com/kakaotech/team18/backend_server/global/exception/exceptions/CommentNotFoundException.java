package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class CommentNotFoundException extends CustomException {

    public CommentNotFoundException(String detail) {
        super(ErrorCode.COMMENT_NOT_FOUND, detail);
    }
}
