package com.kakaotech.team18.backend_server.domain.email.sender;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import com.kakaotech.team18.backend_server.global.exception.exceptions.EmailSendFailedException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.RetryableEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@RequiredArgsConstructor
@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    @Retryable(
            include = { RetryableEmailException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 2_000, multiplier = 2.0, maxDelay = 60_000)
    )
    public void sendHtml(String from, String replyTo, List<String> to, String subject, String htmlBody) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(from);
            helper.setReplyTo(replyTo);
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mime);
        } catch (Exception e) {
            log.info("Email sent failed: replyTo={} to={}", replyTo, to);
            throw new RuntimeException("SMTP send failed", e);
            if (SmtpFailureClassifier.isTemporary(e)) {
                throw new RetryableEmailException("Temporary email failure, will retry", e);
            }
            throw EmailSendFailedException.of(e);
        }
    }
    @Recover
    public void recover(Exception e, String from, String replyTo, List<String> to, String subject, String htmlBody) {
        log.error("[Email] Permanently failed after retries. to={}, subject={}", to, subject, e);
        throw EmailSendFailedException.of(e);
    }
}
