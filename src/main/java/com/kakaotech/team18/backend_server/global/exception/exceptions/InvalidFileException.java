package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidFileException extends CustomException {

  public InvalidFileException(String detail) {
    super(ErrorCode.INVALID_FILE, detail);
  }
}
