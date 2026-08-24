package com.kakaotech.team18.backend_server.domain.email.sender;

import java.util.List;


public interface EmailSender {
    void sendHtml(String from, String replyTo, List<String> to, String subject, String htmlBody);

    /** DB 발송 작업이 재시도를 관리할 때 사용하는 단일 시도 전송입니다. */
    void sendHtmlOnce(String from, String replyTo, List<String> to, String subject, String htmlBody);
}
