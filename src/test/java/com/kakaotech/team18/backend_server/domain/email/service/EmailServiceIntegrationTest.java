package com.kakaotech.team18.backend_server.domain.email.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.user.entity.User;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Disabled("실제 외부 SMTP 메일을 보냅니다. 로컬에서 주석처리 후 수동 실행하세요.")
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean private ClubMemberRepository clubMemberRepository;

    @DisplayName("실제 SMTP 발송")
    @Test
    void sendToApplicant_realSmtp() {
        Club club = Mockito.mock(Club.class);
        given(club.getId()).willReturn(7L);
        given(club.getName()).willReturn("모의동아리");

        ClubApplyForm form = Mockito.mock(ClubApplyForm.class);
        given(form.getClub()).willReturn(club);

        User applicant = Mockito.mock(User.class);
        given(applicant.getName()).willReturn("지원자");
        given(applicant.getEmail()).willReturn(System.getenv().getOrDefault("MAIL_TO", "your-mail"));//본인 이메일 넣어서 테스트 해보세요
        given(applicant.getStudentId()).willReturn("20251234");
        given(applicant.getDepartment()).willReturn("컴퓨터정보");
        given(applicant.getPhoneNumber()).willReturn("010-1111-2222");

        Application application = Mockito.mock(Application.class);
        given(application.getClubApplyForm()).willReturn(form);
        given(application.getUser()).willReturn(applicant);
        given(application.getLastModifiedAt()).willReturn(LocalDateTime.now());

        User president = Mockito.mock(User.class);
        given(president.getEmail()).willReturn(System.getenv().getOrDefault("MAIL_FROM",
                System.getenv().getOrDefault("MAIL_USERNAME", "test@example.com")));

        given(clubMemberRepository.findUserByClubIdAndRoleAndStatus(7L, Role.CLUB_ADMIN, ActiveStatus.ACTIVE))
                .willReturn(Optional.of(president));

        List<AnswerEmailLine> lines = List.of(
                new AnswerEmailLine(1L, 2L, "자기소개", "열심히 하겠습니다."),
                new AnswerEmailLine(2L, 1L, "지원동기", "배우고 기여하겠습니다."),
                new AnswerEmailLine(3L, 3L, "면접 가능 일정", "2025-10-15 14:00,2025-10-16 10:00")
        );

        assertDoesNotThrow(() -> emailService.sendToApplicant(application, lines));
    }
}