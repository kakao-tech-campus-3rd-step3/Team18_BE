package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class CommentAccessDeniedException extends CustomException {

    public CommentAccessDeniedException(String detail) {
        super(ErrorCode.COMMENT_ACCESS_DENIED, detail);
    }
}
