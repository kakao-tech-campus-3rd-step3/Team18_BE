package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class InvalidResultNotificationStageException extends CustomException {

    public InvalidResultNotificationStageException(Stage stage) {
        super(
                ErrorCode.INVALID_INPUT_VALUE,
                "결과 알림 stage는 INTERVIEW 또는 FINAL이어야 합니다. requestedStage=" + stage
        );
    }
}
