package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class S3DeleteException extends CustomException {

  public S3DeleteException(String detail) {
    super(ErrorCode.IO_EXCEPTION, detail);
  }
}
