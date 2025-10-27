package com.kakaotech.team18.backend_server.domain.email.service;

import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationInfoDto;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Disabled("실제 외부 SMTP 메일을 보냅니다. 로컬에서 주석처리 후 수동 실행하세요.")
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @DisplayName("emailservice 부터 전송까지")// 이걸로 LazyInitializationException 발생여부 확인 가능
    @Test
    void sendToApplicant_realSmtp() {
        String to = System.getenv().getOrDefault("MAIL_TO", "your@email.com");
        String replyTo = System.getenv().getOrDefault(
                "MAIL_FROM",
                System.getenv().getOrDefault("MAIL_USERNAME", "test@example.com")
        );

        ApplicationInfoDto info = new ApplicationInfoDto(
                "모의동아리",
                "지원자",
                7L,
                replyTo,
                "20251234",
                "컴퓨터정보",
                "010-1111-2222",
                to,
                LocalDateTime.now()
        );

        List<AnswerEmailLine> lines = List.of(
                new AnswerEmailLine(1L, 2L, "자기소개", "열심히 하겠습니다."),
                new AnswerEmailLine(2L, 1L, "지원동기", "배우고 기여하겠습니다."),
                new AnswerEmailLine(3L, 3L, "면접 가능 일정", "2025-10-15 14:00,2025-10-16 10:00")
        );

        assertDoesNotThrow(() -> emailService.sendToApplicant(info, lines));
    }
}