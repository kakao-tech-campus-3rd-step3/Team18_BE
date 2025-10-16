package com.kakaotech.team18.backend_server.domain.email.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationInfoDto;
import com.kakaotech.team18.backend_server.domain.email.sender.EmailSender;
import com.kakaotech.team18.backend_server.domain.email.template.EmailTemplateRenderer;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.exception.exceptions.PresidentNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest {

    @Mock
    EmailTemplateRenderer renderer;
    @Mock
    EmailSender emailSender;
    @Mock
    ClubMemberRepository clubMemberRepository;

    @Mock
    Application application;
    @Mock
    ClubApplyForm clubApplyForm;
    @Mock
    Club club;
    @Mock
    User applicant;
    @Mock
    User president;

    @Captor
    ArgumentCaptor<Map<String,Object>> modelCaptor;
    @Captor
    ArgumentCaptor<List<String>> toCaptor;

    EmailService service;

    final String from = "no-reply@clubhub.example";

    @BeforeEach
    void setUp() {
        service = new EmailService(renderer, emailSender, from);
    }

    private ApplicationInfoDto infoFixture() {
        return new ApplicationInfoDto(
                "카카오테크 동아리",
                "홍길동",
                77L,
                "president@example.com",
                "20251234",
                "인공지능학과",
                "010-1111-2222",
                "applicant@example.com",
                LocalDateTime.of(2025, 9, 20, 13, 0)
        );
    }

    @Test
    @DisplayName("단위: 메일 전송 성공")
    void unit_success_sendMail() {
        // given
        ApplicationInfoDto info = infoFixture();
        List<AnswerEmailLine> lines = List.of(
                new AnswerEmailLine(2L, 1L, "자기소개", "안녕하세요"),
                new AnswerEmailLine(1L, 2L, "지원 동기", "함께 성장하고 싶습니다")
        );
        when(renderer.render(eq("email-body-applicant"), anyMap()))
                .thenReturn("<html>OK</html>");

        // when
        service.sendToApplicant(info, lines);

        // then
        verify(renderer).render(eq("email-body-applicant"), modelCaptor.capture());
        Map<String, Object> model = modelCaptor.getValue();
        assertThat(model).containsKeys(
                "title", "clubName", "applicantName", "studentId",
                "department", "phoneNumber", "applicantEmail",
                "answers", "submittedAt"
        );
        assertThat(model.get("clubName")).isEqualTo("카카오테크 동아리");
        assertThat(model.get("applicantName")).isEqualTo("홍길동");
        assertThat(model.get("answers")).isEqualTo(lines);

        // then
        final String subjectPrefix = "[동아리 지원]";
        verify(emailSender).sendHtml(
                eq(from),
                eq("president@example.com"),
                toCaptor.capture(),
                eq(subjectPrefix + " " + "카카오테크 동아리" + " - " + "홍길동"),
                eq("<html>OK</html>")
        );
        assertThat(toCaptor.getValue()).containsExactly("applicant@example.com");
    }

    @Test
    @DisplayName("단위: 메일 전송 실패 - EmailSender에서 예외 → 그대로 전파")
    void unit_fail_sendMailThrows() {
        // given
        ApplicationInfoDto info = infoFixture();
        when(renderer.render(eq("email-body-applicant"), anyMap()))
                .thenReturn("<html>OK</html>");
        doThrow(new RuntimeException("SMTP send failed"))
                .when(emailSender)
                .sendHtml(anyString(), anyString(), anyList(), anyString(), anyString());

        // expect
        assertThatThrownBy(() -> service.sendToApplicant(info, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP send failed");

        verify(renderer).render(eq("email-body-applicant"), anyMap());
    }
}
