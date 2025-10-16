package com.kakaotech.team18.backend_server.domain.email.service;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApprovedRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationServiceImpl;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.sender.EmailSender;
import com.kakaotech.team18.backend_server.domain.email.template.EmailTemplateRenderer;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.PendingApplicationsExistException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.PresidentNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    ApplicationRepository applicationRepository;
    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    Application application;
    @Mock
    ClubApplyForm clubApplyForm;
    @Mock
    ClubApplyFormRepository clubApplyFormRepository;
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
    @Captor
    ArgumentCaptor<Object> eventCaptor;
    @Captor
    ArgumentCaptor<List<Application>> appsCaptor;
    @Captor
    ArgumentCaptor<List<Application>> deletedCaptor;

    @InjectMocks
    ApplicationServiceImpl serviceImpl;

    EmailService service;

    final String from = "no-reply@clubhub.example";
    final String subjectPrefix = "[동아리 지원]";

    @BeforeEach
    void setUp() {
        service = new EmailService(renderer, emailSender, from, clubMemberRepository);
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

    @Test
    @DisplayName("INTERVIEW 단계: APPROVED→승격(FINAL)+합격 이벤트, REJECTED→삭제+불합격 이벤트(PENDING 없음)")
    void interview_flow() {
        // given
        Application appApproved = mock(Application.class);
        Application appRejected = mock(Application.class);

        Club club1 = mock(Club.class);
        when(club1.getId()).thenReturn(77L);

        User userApproved = mock(User.class);
        User userRejected = mock(User.class);

        when(userApproved.getEmail()).thenReturn("approved@ex.com");
        when(userRejected.getEmail()).thenReturn("rejected@ex.com");

        when(clubApplyForm.getClub()).thenReturn(club1);

        when(appRejected.getClubApplyForm()).thenReturn(clubApplyForm);

        when(clubApplyFormRepository.findByClubId(77L)).thenReturn(Optional.of(clubApplyForm));

        // 공통: stage 초기값은 INTERVIEW
        final Stage[] approvedStageRef = { Stage.INTERVIEW }; // updateStage 호출 시 바뀌게 함
        when(appApproved.getStage()).thenAnswer(inv -> approvedStageRef[0]);
        doAnswer(inv -> { approvedStageRef[0] = inv.getArgument(0, Stage.class); return null; })
                .when(appApproved).updateStage(any(Stage.class));

        when(appRejected.getStage()).thenReturn(Stage.INTERVIEW);

        // status
        when(appApproved.getStatus()).thenReturn(Status.APPROVED);
        when(appRejected.getStatus()).thenReturn(Status.REJECTED);

        // ids
        when(appApproved.getId()).thenReturn(101L);

        // emails
        when(userApproved.getEmail()).thenReturn("approved@ex.com");
        when(userRejected.getEmail()).thenReturn("rejected@ex.com");
        when(appApproved.getUser()).thenReturn(userApproved);
        when(appRejected.getUser()).thenReturn(userRejected);

        when(applicationRepository.findAllByClubIdAndStage(77L, Stage.INTERVIEW))
                .thenReturn(List.of(appApproved, appRejected));

        ApplicationApprovedRequestDto req = new ApplicationApprovedRequestDto("면접 합격 안내 메시지");

        // when
        SuccessResponseDto resp = serviceImpl.sendPassFailMessage(77L, req, Stage.INTERVIEW);

        //then
        assertThat(resp).isNotNull();

        verify(clubApplyFormRepository).findByClubId(77L);
        verify(clubApplyForm).updateInterviewMessage("면접 합격 안내 메시지");

        // 승격 호출 확인
        verify(appApproved).updateStage(Stage.FINAL);
        verify(appApproved).updateStatus(Status.PENDING);
        verify(appRejected, never()).updateStage(any());

        verify(applicationRepository).deleteAllInBatch(appsCaptor.capture());
        List<Application> deleted = appsCaptor.getValue();
        assertThat(deleted).containsExactly(appRejected);

        // 이벤트 캡처
        verify(publisher, times(2)).publishEvent(eventCaptor.capture());
        List<Object> events = eventCaptor.getAllValues();

        // InterviewApprovedEvent
        InterviewApprovedEvent approvedEvt = events.stream()
                .filter(e -> e instanceof InterviewApprovedEvent)
                .map(e -> (InterviewApprovedEvent) e)
                .findFirst().orElseThrow();
        assertThat(approvedEvt.applicationId()).isEqualTo(101L);
        assertThat(approvedEvt.email()).isEqualTo("approved@ex.com");
        assertThat(approvedEvt.message()).isEqualTo("면접 합격 안내 메시지");
        assertThat(approvedEvt.stage()).isEqualTo(Stage.FINAL);

        // InterviewRejectedEvent
        InterviewRejectedEvent rejectedEvt = events.stream()
                .filter(e -> e instanceof InterviewRejectedEvent)
                .map(e -> (InterviewRejectedEvent) e)
                .findFirst().orElseThrow();
        assertThat(rejectedEvt.user().getEmail()).isEqualTo("rejected@ex.com");
        assertThat(rejectedEvt.club().getId()).isEqualTo(77L);
    }

    @Test
    @DisplayName("FINAL 단계: APPROVED→최종 합격 이벤트, REJECTED→삭제+최종 불합격 이벤트(PENDING 없음)")
    void final_flow() {
        //given
        Application appApproved = mock(Application.class);
        Application appRejected = mock(Application.class);

        Club club1 = mock(Club.class);
        when(club1.getId()).thenReturn(88L);

        User userApproved = mock(User.class);
        User userRejected = mock(User.class);

        when(userApproved.getEmail()).thenReturn("final-approved@ex.com");
        when(userRejected.getEmail()).thenReturn("final-rejected@ex.com");

        when(clubApplyForm.getClub()).thenReturn(club1);

        when(clubApplyFormRepository.findByClubId(88L)).thenReturn(Optional.of(clubApplyForm));

        when(appApproved.getStage()).thenReturn(Stage.FINAL);
        when(appRejected.getStage()).thenReturn(Stage.FINAL);

        when(appApproved.getStatus()).thenReturn(Status.APPROVED);
        when(appRejected.getStatus()).thenReturn(Status.REJECTED);

        when(appApproved.getId()).thenReturn(201L);

        when(appApproved.getUser()).thenReturn(userApproved);
        when(appRejected.getUser()).thenReturn(userRejected);

        when(appRejected.getClubApplyForm()).thenReturn(clubApplyForm);

        when(applicationRepository.findAllByClubIdAndStage(88L, Stage.FINAL))
                .thenReturn(List.of(appApproved, appRejected));

        ApplicationApprovedRequestDto req = new ApplicationApprovedRequestDto("최종 합격 안내 메시지");

        // when
        SuccessResponseDto resp = serviceImpl.sendPassFailMessage(88L, req, Stage.FINAL);

        // then
        assertThat(resp).isNotNull();

        verify(clubApplyFormRepository).findByClubId(88L);
        verify(clubApplyForm).updateFinalMessage("최종 합격 안내 메시지");

        verify(appApproved, never()).updateStage(any());
        verify(appApproved, never()).updateStatus(any());
        verify(appRejected, never()).updateStage(any());
        verify(appRejected, never()).updateStatus(any());

        verify(applicationRepository).deleteAllInBatch(appsCaptor.capture());
        List<Application> deleted = appsCaptor.getValue();
        assertThat(deleted).containsExactly(appRejected);

        // 이벤트 캡처 (approved 1, rejected 1)
        verify(publisher, times(2)).publishEvent(eventCaptor.capture());
        List<Object> events = eventCaptor.getAllValues();

        FinalApprovedEvent approvedEvt = events.stream()
                .filter(e -> e instanceof FinalApprovedEvent)
                .map(e -> (FinalApprovedEvent) e)
                .findFirst().orElseThrow();
        assertThat(approvedEvt.applicationId()).isEqualTo(201L);
        assertThat(approvedEvt.email()).isEqualTo("final-approved@ex.com");
        assertThat(approvedEvt.message()).isEqualTo("최종 합격 안내 메시지");
        assertThat(approvedEvt.stage()).isEqualTo(Stage.FINAL);

        FinalRejectedEvent rejectedEvt = events.stream()
                .filter(e -> e instanceof FinalRejectedEvent)
                .map(e -> (FinalRejectedEvent) e)
                .findFirst().orElseThrow();
        assertThat(rejectedEvt.user().getEmail()).isEqualTo("final-rejected@ex.com");
        assertThat(rejectedEvt.club().getId()).isEqualTo(88L);
    }
    @Test
    @DisplayName("STAGE=NULL: APPROVED/REJECTED 처리 → 최종 이벤트 발행 + REJECTED 배치 삭제, 메시지 업데이트 없음")
    void nullStage_flow_success() {
        // given
        Long clubId = 99L;

        Club clubN = mock(Club.class);
        when(clubN.getId()).thenReturn(clubId);
        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(clubApplyForm));
        when(clubApplyForm.getClub()).thenReturn(clubN);

        // 앱들 (모두 stage == null)
        Application appApproved = mock(Application.class);
        Application appRejected = mock(Application.class);

        when(appApproved.getStage()).thenReturn(null);

        when(appApproved.getStatus()).thenReturn(Status.APPROVED);
        when(appRejected.getStatus()).thenReturn(Status.REJECTED);

        when(appApproved.getId()).thenReturn(301L);

        User userApproved = mock(User.class);
        User userRejected = mock(User.class);
        when(userApproved.getEmail()).thenReturn("nullstage-approved@ex.com");
        when(userRejected.getEmail()).thenReturn("nullstage-rejected@ex.com");

        when(appApproved.getUser()).thenReturn(userApproved);
        when(appRejected.getUser()).thenReturn(userRejected);

        when(appRejected.getClubApplyForm()).thenReturn(clubApplyForm);

        when(applicationRepository.findAllByClubId(clubId))
                .thenReturn(List.of(appApproved, appRejected));

        ApplicationApprovedRequestDto req = new ApplicationApprovedRequestDto("공지 메시지");

        // when
        SuccessResponseDto resp = serviceImpl.sendPassFailMessage(clubId, req, null);

        // then
        assertThat(resp).isNotNull();

        // 메시지 템플릿 업데이트는 호출되지 않아야 함
        verify(clubApplyForm, never()).updateInterviewMessage(anyString());
        verify(clubApplyForm, never()).updateFinalMessage(anyString());

        // stage/status 변경 없음
        verify(appApproved, never()).updateStage(any());
        verify(appApproved, never()).updateStatus(any());
        verify(appRejected, never()).updateStage(any());
        verify(appRejected, never()).updateStatus(any());

        // REJECTED만 배치 삭제
        verify(applicationRepository).deleteAllInBatch(appsCaptor.capture());
        List<Application> deleted = appsCaptor.getValue();
        assertThat(deleted).containsExactly(appRejected);

        // 이벤트 2건 (approved 1, rejected 1)
        verify(publisher, times(2)).publishEvent(eventCaptor.capture());
        List<Object> events = eventCaptor.getAllValues();

        // FinalApprovedEvent (stage 가 null 이어야 함)
        FinalApprovedEvent approvedEvt = events.stream()
                .filter(e -> e instanceof FinalApprovedEvent)
                .map(e -> (FinalApprovedEvent) e)
                .findFirst().orElseThrow();
        assertThat(approvedEvt.applicationId()).isEqualTo(301L);
        assertThat(approvedEvt.email()).isEqualTo("nullstage-approved@ex.com");
        assertThat(approvedEvt.message()).isEqualTo("공지 메시지");
        assertThat(approvedEvt.stage()).isNull();

        // FinalRejectedEvent (club/user 페이로드 확인)
        FinalRejectedEvent rejectedEvt = events.stream()
                .filter(e -> e instanceof FinalRejectedEvent)
                .map(e -> (FinalRejectedEvent) e)
                .findFirst().orElseThrow();
        assertThat(rejectedEvt.user().getEmail()).isEqualTo("nullstage-rejected@ex.com");
        assertThat(rejectedEvt.club().getId()).isEqualTo(clubId);
    }

    @Test
    @DisplayName("stage=interview, PENDING 존재 시 PendingApplicationsExistException 던지고 아무 것도 하지 않음")
    void interviewStage_flow_pending_exists_throws() {
        // given
        Long clubId = 100L;

        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(clubApplyForm));

        Application appPending = mock(Application.class);
        when(appPending.getStage()).thenReturn(Stage.INTERVIEW);
        when(appPending.getStatus()).thenReturn(Status.PENDING);

        when(applicationRepository.findAllByClubIdAndStage(clubId, Stage.INTERVIEW))
                .thenReturn(List.of(appPending));

        ApplicationApprovedRequestDto req = new ApplicationApprovedRequestDto("message");

        // when / then
        assertThatThrownBy(() -> serviceImpl.sendPassFailMessage(clubId, req, Stage.INTERVIEW))
                .isInstanceOf(PendingApplicationsExistException.class);

        // 부수효과 없음
        verify(publisher, never()).publishEvent(any());
        verify(applicationRepository, never()).deleteAllInBatch(any());
        verify(clubApplyForm, never()).updateInterviewMessage(anyString());
        verify(clubApplyForm, never()).updateFinalMessage(anyString());
    }

    @Test
    @DisplayName("stage=final, PENDING 존재 시 PendingApplicationsExistException 던지고 아무 것도 하지 않음")
    void finalStage_flow_pending_exists_throws() {
        // given
        Long clubId = 100L;

        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(clubApplyForm));

        Application appPending = mock(Application.class);
        when(appPending.getStage()).thenReturn(Stage.FINAL);
        when(appPending.getStatus()).thenReturn(Status.PENDING);

        when(applicationRepository.findAllByClubIdAndStage(clubId, Stage.FINAL))
                .thenReturn(List.of(appPending));

        ApplicationApprovedRequestDto req = new ApplicationApprovedRequestDto("message");

        // when / then
        assertThatThrownBy(() -> serviceImpl.sendPassFailMessage(clubId, req, Stage.FINAL))
                .isInstanceOf(PendingApplicationsExistException.class);

        // 부수효과 없음
        verify(publisher, never()).publishEvent(any());
        verify(applicationRepository, never()).deleteAllInBatch(any());
        verify(clubApplyForm, never()).updateInterviewMessage(anyString());
        verify(clubApplyForm, never()).updateFinalMessage(anyString());
    }

    @Test
    @DisplayName("stage=null, PENDING 존재 시 PendingApplicationsExistException 던지고 아무 것도 하지 않음")
    void nullStage_flow_pending_exists_throws() {
        // given
        Long clubId = 100L;

        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(clubApplyForm));

        Application appPending = mock(Application.class);
        when(appPending.getStatus()).thenReturn(Status.PENDING);

        when(applicationRepository.findAllByClubId(clubId))
                .thenReturn(List.of(appPending));

        ApplicationApprovedRequestDto req = new ApplicationApprovedRequestDto("message");

        // when / then
        assertThatThrownBy(() -> serviceImpl.sendPassFailMessage(clubId, req, null))
                .isInstanceOf(PendingApplicationsExistException.class);

        // 부수효과 없음
        verify(publisher, never()).publishEvent(any());
        verify(applicationRepository, never()).deleteAllInBatch(any());
        verify(clubApplyForm, never()).updateInterviewMessage(anyString());
        verify(clubApplyForm, never()).updateFinalMessage(anyString());
    }

}
