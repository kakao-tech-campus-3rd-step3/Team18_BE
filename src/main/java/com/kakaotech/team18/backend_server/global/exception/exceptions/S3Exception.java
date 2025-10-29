package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class S3Exception extends CustomException {

  public S3Exception(String detail) {
    super(ErrorCode.AWS_EXCEPTION, detail);
  }
}
