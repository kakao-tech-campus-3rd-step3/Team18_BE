package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class FileNotFoundException extends CustomException {
    public FileNotFoundException(String message) {
        super(ErrorCode.FILE_NOT_FOUND, message);
    }
}
