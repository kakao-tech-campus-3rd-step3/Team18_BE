package com.kakaotech.team18.backend_server.domain.email.sender;

import java.util.List;


public interface EmailSender {
    void sendHtml(String from, String replyTo, List<String> to, String subject, String htmlBody);
}
