package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.domain.email.sender.SmtpFailureClassifier;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import jakarta.mail.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.eclipse.angus.mail.util.MailConnectException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import java.net.SocketTimeoutException;

@Slf4j
public class EmailSendFailedException extends CustomException {
    public EmailSendFailedException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail);
        if (cause != null) initCause(cause);
    }

    public static EmailSendFailedException of(Throwable cause) {
        ErrorCode code = SmtpFailureClassifier.toErrorCode(cause);
        return new EmailSendFailedException(code, brief(cause), cause);
    }

    private static String brief(Throwable cause) {
        String m = (cause == null ? null : cause.getMessage());
        return (m == null || m.isBlank()) ? "이메일 전송 실패" : m;
    }
}