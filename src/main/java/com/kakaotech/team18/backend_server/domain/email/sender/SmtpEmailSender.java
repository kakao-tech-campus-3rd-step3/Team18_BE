package com.kakaotech.team18.backend_server.domain.email.sender;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    public SmtpEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
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
        }
    }
}
