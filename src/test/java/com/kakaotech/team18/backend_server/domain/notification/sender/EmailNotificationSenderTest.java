package com.kakaotech.team18.backend_server.domain.notification.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.exception.NotificationSendException;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.EmailSendFailedException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.RetryableEmailException;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderTest {

    @Mock
    private EmailService emailService;

    private EmailNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new EmailNotificationSender(emailService);
    }

    @Test
    void sendsStoredEmailOnceAndReturnsSent() {
        NotificationMessage message = message();

        NotificationSendResult result = sender.send(message);

        verify(emailService).sendStoredResultNotificationOnce(
                "president@example.com",
                "applicant@example.com",
                "결과 안내",
                "합격을 축하드립니다."
        );
        assertThat(result.outcome()).isEqualTo(NotificationSendResult.Outcome.SENT);
        assertThat(result.providerStatusCode()).isEqualTo("SMTP_SENT");
    }

    @Test
    void mapsTemporarySmtpFailureToRetryableFailure() {
        RetryableEmailException failure = new RetryableEmailException("timeout", new RuntimeException());
        doThrow(failure).when(emailService).sendStoredResultNotificationOnce(
                "president@example.com",
                "applicant@example.com",
                "결과 안내",
                "합격을 축하드립니다."
        );

        assertThatThrownBy(() -> sender.send(message()))
                .isInstanceOfSatisfying(NotificationSendException.class, exception -> {
                    assertThat(exception.getDisposition())
                            .isEqualTo(NotificationSendException.FailureDisposition.RETRYABLE);
                    assertThat(exception.getErrorCode()).isEqualTo("EMAIL_TEMPORARY_FAILURE");
                });
    }

    @Test
    void mapsPermanentSmtpFailureToPermanentFailure() {
        EmailSendFailedException failure = new EmailSendFailedException(
                ErrorCode.EMAIL_AUTH_FAILED,
                "invalid credentials",
                null
        );
        doThrow(failure).when(emailService).sendStoredResultNotificationOnce(
                "president@example.com",
                "applicant@example.com",
                "결과 안내",
                "합격을 축하드립니다."
        );

        assertThatThrownBy(() -> sender.send(message()))
                .isInstanceOfSatisfying(NotificationSendException.class, exception -> {
                    assertThat(exception.getDisposition())
                            .isEqualTo(NotificationSendException.FailureDisposition.PERMANENT);
                    assertThat(exception.getErrorCode()).isEqualTo("EMAIL_AUTH_FAILED");
                });
    }

    @Test
    void mapsSmtpTimeoutToUnknownToPreventDuplicateEmail() {
        RetryableEmailException failure = new RetryableEmailException(
                "read timed out",
                new SocketTimeoutException("read timed out")
        );
        doThrow(failure).when(emailService).sendStoredResultNotificationOnce(
                "president@example.com",
                "applicant@example.com",
                "결과 안내",
                "합격을 축하드립니다."
        );

        assertThatThrownBy(() -> sender.send(message()))
                .isInstanceOfSatisfying(NotificationSendException.class, exception -> {
                    assertThat(exception.getDisposition())
                            .isEqualTo(NotificationSendException.FailureDisposition.UNKNOWN);
                    assertThat(exception.getErrorCode()).isEqualTo("EMAIL_TIMEOUT");
                });
    }

    private NotificationMessage message() {
        return new NotificationMessage(
                1L,
                NotificationChannel.EMAIL,
                "applicant@example.com",
                "president@example.com",
                "결과 안내",
                "합격을 축하드립니다."
        );
    }
}
