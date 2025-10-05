package com.kakaotech.team18.backend_server.domain.email.sender;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import org.eclipse.angus.mail.smtp.SMTPAddressFailedException;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.eclipse.angus.mail.util.MailConnectException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SmtpFailureClassifier {

    private SmtpFailureClassifier() {}

    private static final Pattern ENHANCED = Pattern.compile("(\\d\\.\\d\\.\\d{1,3})");

    // 재시도 대상
    public static boolean isTemporary(Throwable t) {
        Throwable root = unwrapRoot(t);

        if (root instanceof MailConnectException || root instanceof SocketTimeoutException) return true;
        if (root instanceof MailAuthenticationException) return false;

        Integer basic = null;
        String enhanced = null;
        String msg = root.getMessage() == null ? "" : root.getMessage();

        if (root instanceof SMTPSendFailedException ssfe) {
            basic = ssfe.getReturnCode();
            enhanced = extractEnhanced(msg);
        } else if (root instanceof SMTPAddressFailedException safe) {
            basic = safe.getReturnCode();
            enhanced = extractEnhanced(msg);
        } else if (root instanceof SendFailedException sfe) {
            basic = firstReplyFromChain(sfe);
            enhanced = extractEnhanced(msg);
        } else if (root instanceof MailSendException mse && !mse.getFailedMessages().isEmpty()) {
            MessagingException me = (MessagingException) mse.getFailedMessages().values().stream().findFirst().orElse(null);
            if (me != null) {
                String m = me.getMessage() == null ? "" : me.getMessage();
                enhanced = extractEnhanced(m);
            }
        }

        if (enhanced != null) {
            if (enhanced.startsWith("4.")) return true;      // 4.x.x = 임시
            if ("5.2.2".equals(enhanced)) return true;       // mailbox full
            return false;                                     // 그 외 5.x.x
        }

        if (basic != null) {
            if (basic >= 400 && basic < 500) return true;    // 4xx
            if (basic == 552) return true;                   // 용량/스토리지
            return false;                                    // 나머지 5xx
        }

        String lower = msg.toLowerCase();
        return lower.contains("try again later")
                || lower.contains("temporarily")
                || lower.contains("timeout")
                || lower.contains("over quota");
    }

    // 예외단: RFC 5321 + 3463 기반 ErrorCode 매핑
    public static ErrorCode toErrorCode(Throwable t) {
        Throwable root = unwrapRoot(t);

        if (root instanceof MailAuthenticationException) return ErrorCode.EMAIL_AUTH_FAILED;
        if (root instanceof MailConnectException)        return ErrorCode.EMAIL_TEMPORARY_FAILURE;
        if (root instanceof SocketTimeoutException)      return ErrorCode.EMAIL_TIMEOUT;

        int rc = -1;
        String enhanced = null;
        String msg = root.getMessage() == null ? "" : root.getMessage();

        if (root instanceof SMTPSendFailedException ssfe) {
            rc = ssfe.getReturnCode();
            enhanced = extractEnhanced(msg);
        } else if (root instanceof SMTPAddressFailedException safe) {
            rc = safe.getReturnCode();
            enhanced = extractEnhanced(msg);
        } else if (root instanceof SendFailedException sfe) {
            rc = firstReplyFromChain(sfe);
            enhanced = extractEnhanced(msg);
        }

        if (enhanced != null) {
            switch (enhanced) {
                case "5.1.0", "5.1.1", "5.1.2": return ErrorCode.EMAIL_RECIPIENT_INVALID;
                case "5.2.2":                   return ErrorCode.EMAIL_TEMPORARY_FAILURE;
                case "5.4.1", "4.4.1":          return ErrorCode.EMAIL_TEMPORARY_FAILURE;
                case "5.7.1", "5.7.25":         return ErrorCode.EMAIL_POLICY_REJECTED;
                default:                         break;
            }
        }

        if (rc >= 400 && rc < 500) return ErrorCode.EMAIL_TEMPORARY_FAILURE;
        if (rc == 550 || rc == 551 || rc == 553) return ErrorCode.EMAIL_RECIPIENT_INVALID;
        if (rc == 552) return ErrorCode.EMAIL_TEMPORARY_FAILURE;
        if (rc == 535 || rc == 530) return ErrorCode.EMAIL_AUTH_FAILED;
        if (rc == 554) return ErrorCode.EMAIL_POLICY_REJECTED;
        if (rc >= 500) return ErrorCode.EMAIL_SEND_FAILED;

        return ErrorCode.EMAIL_SEND_FAILED;
    }

    // helpers
    private static String extractEnhanced(String message) {
        if (message == null) return null;
        Matcher m = ENHANCED.matcher(message);
        return m.find() ? m.group(1) : null;
    }

    private static Integer firstReplyFromChain(SendFailedException sfe) {
        Throwable n = sfe;
        while (n != null) {
            switch (n) {
                case SMTPAddressFailedException safe -> {
                    return safe.getReturnCode();
                }
                case SMTPSendFailedException ssfe -> {
                    return ssfe.getReturnCode();
                }
                case MessagingException me -> {
                    n = me.getNextException();
                    continue;
                }
                default -> {
                }
            }
            n = n.getCause();
        }
        return null;
    }

    private static Throwable unwrapRoot(Throwable t) {
        if (t instanceof MailSendException mse && !mse.getFailedMessages().isEmpty()) {
            return mse.getFailedMessages().values().iterator().next();
        }
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}
