package com.kakaotech.team18.backend_server.domain.notification.sender;

import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import com.kakaotech.team18.backend_server.domain.email.sender.SmtpFailureClassifier;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.exception.NotificationSendException;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.global.exception.exceptions.EmailSendFailedException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.RetryableEmailException;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final EmailService emailService;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public NotificationSendResult send(NotificationMessage message) {
        try {
            emailService.sendStoredResultNotificationOnce(
                    message.replyToAddress(),
                    message.recipientAddress(),
                    message.subject(),
                    message.body()
            );
            return NotificationSendResult.sent("SMTP_SENT");
        } catch (RetryableEmailException exception) {
            ErrorCode errorCode = SmtpFailureClassifier.toErrorCode(exception);
            if (errorCode == ErrorCode.EMAIL_TIMEOUT) {
                throw NotificationSendException.unknown(
                        errorCode.name(),
                        safeMessage(exception),
                        exception
                );
            }
            String retryableErrorCode = errorCode == ErrorCode.EMAIL_SEND_FAILED
                    ? ErrorCode.EMAIL_TEMPORARY_FAILURE.name()
                    : errorCode.name();
            throw NotificationSendException.retryable(
                    retryableErrorCode,
                    safeMessage(exception),
                    exception
            );
        } catch (EmailSendFailedException exception) {
            throw NotificationSendException.permanent(
                    exception.getErrorCode().name(),
                    exception.getDetail(),
                    exception
            );
        }
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? "이메일 임시 전송 실패" : exception.getMessage();
    }
}
