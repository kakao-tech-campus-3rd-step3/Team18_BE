package com.kakaotech.team18.backend_server.domain.email.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
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
class EmailServiceTest {

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
    final String subjectPrefix = "[동아리 지원서]";

    @BeforeEach
    void setUp() {
        service = new EmailService(renderer, emailSender, from, subjectPrefix, clubMemberRepository);
    }

    private void stubHappyPath() {
        when(application.getLastModifiedAt()).thenReturn(LocalDateTime.of(2025,9,20,13,0));
        when(application.getUser()).thenReturn(applicant);
        when(applicant.getName()).thenReturn("홍길동");
        when(applicant.getStudentId()).thenReturn("20251234");
        when(applicant.getDepartment()).thenReturn("인공지능학과");
        when(applicant.getPhoneNumber()).thenReturn("010-1111-2222");
        when(applicant.getEmail()).thenReturn("applicant@example.com");

        when(application.getClubApplyForm()).thenReturn(clubApplyForm);
        when(clubApplyForm.getClub()).thenReturn(club);
        when(club.getId()).thenReturn(77L);
        when(club.getName()).thenReturn("카카오테크 동아리");

        when(clubMemberRepository.findUserByClubIdAndRoleAndStatus(77L, Role.CLUB_ADMIN, ActiveStatus.ACTIVE))
                .thenReturn(Optional.of(president));
        when(president.getEmail()).thenReturn("president@example.com");

        when(renderer.render(eq("email-body-applicant"), anyMap())).thenReturn("<html>OK</html>");
    }

    @Test
    @DisplayName("단위: 메일 전송 성공")
    void unit_success_sendMail() {
        stubHappyPath();
        List<AnswerEmailLine> lines = List.of(
                new AnswerEmailLine(2L, 1L, "자기소개", "안녕하세요"),
                new AnswerEmailLine(1L, 2L, "지원 동기", "함께 성장하고 싶습니다")
        );

        service.sendToApplicant(application, lines);

        // 템플릿 렌더링 모델 검증
        verify(renderer).render(eq("email-body-applicant"), modelCaptor.capture());
        Map<String,Object> model = modelCaptor.getValue();
        assertThat(model).containsKeys("title","clubName","applicantName","studentId","department",
                "phoneNumber","applicantEmail","answers","submittedAt");
        assertThat(model.get("clubName")).isEqualTo("카카오테크 동아리");
        assertThat(model.get("applicantName")).isEqualTo("홍길동");
        assertThat(model.get("answers")).isEqualTo(lines);

        // 메일 전송 인자 검증
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
        stubHappyPath();
        doThrow(new RuntimeException("SMTP send failed"))
                .when(emailSender)
                .sendHtml(anyString(), anyString(), anyList(), anyString(), anyString());

        assertThatThrownBy(() -> service.sendToApplicant(application, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP send failed");

        // 실패 시에도 renderer는 호출돼서 html 생성까지 시도했는지
        verify(renderer).render(eq("email-body-applicant"), anyMap());
    }

    @Test
    @DisplayName("단위: 회장 조회 실패 - PresidentNotFoundException")
    void unit_fail_noPresident() {
        when(application.getClubApplyForm()).thenReturn(clubApplyForm);
        when(clubApplyForm.getClub()).thenReturn(club);
        when(club.getId()).thenReturn(77L);
        when(clubMemberRepository.findUserByClubIdAndRoleAndStatus(77L, Role.CLUB_ADMIN, ActiveStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendToApplicant(application, List.of()))
                .isInstanceOf(PresidentNotFoundException.class)
                .hasMessageContaining("해당 동아리의 회장이 없습니다");

        verify(emailSender, never()).sendHtml(anyString(), anyString(), anyList(), anyString(), anyString());
    }
}
