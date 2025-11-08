package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidClubNameException extends CustomException {

  public InvalidClubNameException(String detail) {
    super(ErrorCode.INVALID_CLUB_NAME, detail);
  }
}