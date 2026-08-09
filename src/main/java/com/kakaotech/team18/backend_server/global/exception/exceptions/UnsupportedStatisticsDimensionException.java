package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class UnsupportedStatisticsDimensionException extends CustomException {

    public UnsupportedStatisticsDimensionException(String detail) {
        super(ErrorCode.UNSUPPORTED_STATISTICS_DIMENSION, detail);
    }
}
