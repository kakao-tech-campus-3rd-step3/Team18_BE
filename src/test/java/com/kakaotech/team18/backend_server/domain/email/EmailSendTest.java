package com.kakaotech.team18.backend_server.domain.email;

import com.kakaotech.team18.backend_server.domain.email.sender.EmailSender;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class EmailSendTest {

    @Nested
    @Disabled("실제 외부로 SMTP 메일을 보냅니다. 로컬에서 주석처리하고 수동으로만 실행하세요.")
    class SmtpTests {

        @Autowired
        private EmailSender emailSender;

        @DisplayName("기본 이메일 전송 테스트")
        @Test
        void smtpTest() {
            String to = System.getenv().getOrDefault("MAIL_TO", "본인_이메일");//<=여기다 본인 이메일 넣으면 확인 가능
            String from = System.getenv().getOrDefault("MAIL_FROM",
                    System.getenv().getOrDefault("MAIL_USERNAME", "test@example.com"));
            String replyTo = System.getenv().getOrDefault("MAIL_REPLY_TO", from);

            String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
            String subject = "[전송 테스트] SMTP send - " + unique;

            String html = """
                    <h3>SMTP 실제 테스트</h3>
                    <p>unique = %s</p>
                    <p>이 메일로 작동 확인함</p>
                    """.formatted(unique);

            assertDoesNotThrow(() -> emailSender.sendHtml(from, replyTo, java.util.List.of(to), subject, html)
            );
        }
    }
}
