package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import jakarta.mail.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.eclipse.angus.mail.util.MailConnectException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import java.net.SocketTimeoutException;

//에러코드를 gmail 쓴다는 가정으로 만들었으니, 다른 것을 사용하면 검토할 필요가 있음
@Slf4j
public class EmailSendFailedException extends CustomException {
    public EmailSendFailedException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail);
        if (cause != null) initCause(cause);
    }

    public static EmailSendFailedException of(Throwable cause) {
        ErrorCode code = classifyForGmail(cause);
        return new EmailSendFailedException(code, brief(cause), cause);
    }

    private static ErrorCode classifyForGmail(Throwable t) {
        Throwable root = unwrapRoot(t);

        // 1) 인증 실패 (앱 비밀번호/자격증명 오류)
        if (root instanceof MailAuthenticationException || root instanceof AuthenticationFailedException) {
            return ErrorCode.EMAIL_AUTH_FAILED;
        }

        // 2) Gmail이 반환하는 SMTP 코드 기반 분류
        if (root instanceof SMTPSendFailedException smtp) {
            int rc = smtp.getReturnCode();
            String msg = String.valueOf(smtp.getMessage()).toLowerCase();

            // 수신자 없음: 550 5.1.1
            if (rc == 550 && msg.contains("5.1.1")) {
                return ErrorCode.EMAIL_RECIPIENT_INVALID;
            }
            // 정책/스팸/DMARC: 550 5.7.1
            if (rc == 550 && msg.contains("5.7.1")) {
                log.error(msg);
                return ErrorCode.EMAIL_POLICY_REJECTED;
            }
            // 임시 실패/레이트 리밋/용량: 421/450/451/452 또는 4.7.0 / over quota / try again later
            if (rc == 421 || rc == 450 || rc == 451 || rc == 452
                    || msg.contains("4.7.0") || msg.contains("try again later") || msg.contains("over quota")) {
                log.error(msg);
                return ErrorCode.EMAIL_TEMPORARY_FAILURE;
            }
            // 사용자/비밀번호 거절 문구가 SMTP 예외 메시지에 포함된 경우
            if (msg.contains("username and password not accepted") || msg.contains("application-specific password")) {
                log.error(msg);
                return ErrorCode.EMAIL_AUTH_FAILED;
            }
        }

        // 3) 연결 불가도 Gmail 측 일시적 이슈로 간주
        if (root instanceof MailConnectException) {
            log.error(root.getMessage(), root.getCause());
            return ErrorCode.EMAIL_TEMPORARY_FAILURE;
        }

        // 4) 타임아웃
        if (root instanceof SocketTimeoutException) {
            log.error(root.getMessage(), root.getCause());
            return ErrorCode.EMAIL_TIMEOUT;
        }

        // 5) 그 외는 일반 실패
        return ErrorCode.EMAIL_SEND_FAILED;
    }

    private static Throwable unwrapRoot(Throwable t) {
        if (t instanceof MailSendException mse && !mse.getFailedMessages().isEmpty()) {
            return mse.getFailedMessages().values().iterator().next();
        }
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }

    private static String brief(Throwable cause) {
        String m = (cause == null ? null : cause.getMessage());
        return (m == null || m.isBlank()) ? "이메일 전송 실패" : m;
    }
}
