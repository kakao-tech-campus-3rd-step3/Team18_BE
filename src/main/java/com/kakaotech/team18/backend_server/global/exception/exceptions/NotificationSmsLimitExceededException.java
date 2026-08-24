package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class NotificationSmsLimitExceededException extends CustomException {

    public NotificationSmsLimitExceededException(int requestedCount, int maxCount) {
        super(
                ErrorCode.NOTIFICATION_SMS_LIMIT_EXCEEDED,
                "requestedCount=%d, maxCount=%d".formatted(requestedCount, maxCount)
        );
    }
}
