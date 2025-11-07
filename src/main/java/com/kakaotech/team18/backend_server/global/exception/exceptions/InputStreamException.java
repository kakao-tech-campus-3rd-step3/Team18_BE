package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InputStreamException extends CustomException {

  public InputStreamException(String message) {
    super(ErrorCode.IO_EXCEPTION, message);
  }
}
