package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class AwsS3Exception extends CustomException {

  public AwsS3Exception(String detail) {
    super(ErrorCode.AWS_EXCEPTION, detail);
  }
}
