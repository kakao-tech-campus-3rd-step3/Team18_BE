package com.kakaotech.team18.backend_server.domain.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto.AnswerDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationFixedInterviewRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.InterviewPreference;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.notification.repository.ResultNotificationRequestRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.ResultNotificationDeliveryService;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import com.kakaotech.team18.backend_server.global.util.DateUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private ClubApplyFormRepository clubApplyFormRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubMemberRepository clubMemberRepository;

    @Mock
    private FormQuestionRepository formQuestionRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private ResultNotificationRequestRepository resultNotificationRequestRepository;

    @Mock
    private ResultNotificationDeliveryService resultNotificationDeliveryService;

    @Test
    @DisplayName("지원서 상세 조회 - 성공 (여러 지원서 중 특정 지원서 조회)")
    void getApplicationDetail_success_whenMultipleApplicationsExist() {
        // given
        Long clubId = 1L;
        Long userId = 1L;
        Long applicantId = 1L;

        User mockUser = User.builder()
                .name("김지원")
                .department("컴퓨터공학과")
                .studentId("20230001")
                .email("test@test.com")
                .phoneNumber("010-1234-5678")
                .build();
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockUser, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ClubApplyForm mockClubApplyForm = mock(ClubApplyForm.class);
        Application mockApplication = mock(Application.class);
        FormQuestion mockQuestion = mock(FormQuestion.class);
        Answer mockAnswer = mock(Answer.class);

        Long mockFormId = 10L;
        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(mockClubApplyForm));
        when(applicationRepository.findById(applicantId)).thenReturn(Optional.of(mockApplication));

        when(answerRepository.findByApplicationWithFormQuestion(mockApplication)).thenReturn(List.of(mockAnswer));
        when(mockApplication.getId()).thenReturn(100L);
        when(mockApplication.getStatus()).thenReturn(Status.PENDING);
        when(mockApplication.getUser()).thenReturn(mockUser);
        when(mockAnswer.getFormQuestion()).thenReturn(mockQuestion);
        when(mockQuestion.getQuestion()).thenReturn("리팩토링된 질문");
        when(mockAnswer.getAnswer()).thenReturn("리팩토링된 답변");

        // when
        ApplicationDetailResponseDto result = applicationService.getApplicationDetail(clubId, applicantId);

        // then
        assertNotNull(result);
        assertEquals(100L, result.applicationId());
        assertEquals("PENDING", result.status());

        assertEquals(userId, result.applicantInfo().applicantId());
        assertEquals("김지원", result.applicantInfo().name());
        assertEquals("컴퓨터공학과", result.applicantInfo().department());

        assertEquals(1, result.questionsAndAnswers().size());
        assertEquals("리팩토링된 질문", result.questionsAndAnswers().get(0).question());
        assertEquals("리팩토링된 답변", result.questionsAndAnswers().get(0).answer());

        verify(clubApplyFormRepository, times(1)).findByClubId(clubId);
        verify(applicationRepository, times(1)).findById(applicantId);
        verify(answerRepository, times(1)).findByApplicationWithFormQuestion(mockApplication);
    }

    @Test
    @DisplayName("지원서 상세 조회 - 실패 (지원서는 존재하지 않음)")
    void getApplicationDetail_fail_applicationNotFound() {
        // given
        Long clubId = 1L;
        Long userId = 999L;
        Long applicantId = 999L;
        Long formId = 10L;

        ClubApplyForm mockClubApplyForm = mock(ClubApplyForm.class);
        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(mockClubApplyForm));

        when(applicationRepository.findById(applicantId)).thenReturn(Optional.empty());

        // when & then
        ApplicationNotFoundException exception = assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.getApplicationDetail(clubId, userId);
        });

        assertEquals("해당 지원서를 찾을 수 없습니다.", exception.getMessage());

        // verify
        verify(clubApplyFormRepository, times(1)).findByClubId(clubId);
        verify(applicationRepository, times(1)).findById(applicantId);
        verify(answerRepository, never()).findByApplicationWithFormQuestion(any(Application.class));
    }

    @Test
    @DisplayName("지원서 상세 조회 - 실패 (지원서 양식을 찾을 수 없음)")
    void getApplicationDetail_fail_clubApplyFormNotFound() {
        // given
        Long clubId = 1L;
        Long userId = 1L;

        // clubApplyFormRepository.findByClubId가 호출되면 Optional.empty()를 반환하도록 설정
        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.empty());

        // when & then
        // getApplicationDetail을 호출했을 때 ClubApplyFormNotFoundException이 발생하는지 검증
        ClubApplyFormNotFoundException exception = assertThrows(ClubApplyFormNotFoundException.class, () -> {
            applicationService.getApplicationDetail(clubId, userId);
        });

        assertEquals("지원폼이 존재하지 않습니다", exception.getMessage());

        // verify
        verify(clubApplyFormRepository, times(1)).findByClubId(clubId);
        verify(applicationRepository, never()).findByClubApplyFormIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("지원서 상태 변경 - 성공 (로그 검증)")
    void updateApplicationStatus_success() {
        // given
        Long applicationId = 1L;
        Status oldStatus = Status.PENDING;
        Status newStatus = Status.APPROVED;
        ApplicationStatusUpdateRequestDto requestDto = new ApplicationStatusUpdateRequestDto(newStatus);

        Application mockApplication = mock(Application.class);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(mockApplication));
        when(mockApplication.getStatus()).thenReturn(oldStatus, newStatus);

        Logger logger = (Logger) LoggerFactory.getLogger(ApplicationServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        // when
        SuccessResponseDto responseDto = applicationService.updateApplicationStatus(applicationId, requestDto);

        // then
        assertTrue(responseDto.success());
        verify(applicationRepository, times(1)).findById(applicationId);
        verify(mockApplication, times(1)).updateStatus(newStatus);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(2, logsList.size());
        assertEquals("지원서 상태 변경 시작: applicationId=1, oldStatus=PENDING, newStatus=APPROVED", logsList.get(0).getFormattedMessage());
        assertEquals("지원서 상태 변경 완료: applicationId=1, newStatus=APPROVED", logsList.get(1).getFormattedMessage());

        logger.detachAndStopAllAppenders();
    }

    @Test
    @DisplayName("지원서 상태 변경 - 실패 (지원서 없음)")
    void updateApplicationStatus_fail_applicationNotFound() {
        // given
        Long nonExistentApplicationId = 999L;
        ApplicationStatusUpdateRequestDto requestDto = new ApplicationStatusUpdateRequestDto(Status.APPROVED);

        when(applicationRepository.findById(nonExistentApplicationId)).thenReturn(Optional.empty());

        // when & then
        ApplicationNotFoundException exception = assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.updateApplicationStatus(nonExistentApplicationId, requestDto);
        });

        assertEquals("applicationId: " + nonExistentApplicationId, exception.getDetail());
        verify(applicationRepository, times(1)).findById(nonExistentApplicationId);
    }

    @Test
    @DisplayName("지원서 제출 - 성공 (면접 시간 파싱 및 저장 검증)")
    void submitApplication_success_withInterviewTimeParsing() {
        // given
        Long clubId = 1L;
        String studentId = "20230001";
        String interviewTimeRaw = "2025-10-16 10:30-11:00,2025-10-16 11:00-11:30";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode interviewTimeNode = objectMapper.valueToTree(interviewTimeRaw);

        // questionNum을 0L로 설정 (FormQuestion의 displayOrder가 1L일 때, 서비스 로직에서 disp-1 = 0을 조회하므로)
        AnswerDto answerDto = new AnswerDto(0L, "면접 가능 날짜는?", interviewTimeNode);
        ApplicationApplyRequestDto requestDto = new ApplicationApplyRequestDto(
                "test@test.com", "김지원", studentId, "010-1234-5678", "컴퓨터공학과", null, null, List.of(answerDto)
        );

        ClubApplyForm mockForm = mock(ClubApplyForm.class);
        Club mockClub = mock(Club.class);
        when(mockForm.getClub()).thenReturn(mockClub);
        when(mockForm.getId()).thenReturn(10L);
        when(mockClub.getId()).thenReturn(clubId);

        User mockUser = User.builder()
                .studentId(studentId)
                .email("test@test.com")
                .name("김지원")
                .phoneNumber("010-1234-5678")
                .department("컴퓨터공학과")
                .build();

        User mockPresident = mock(User.class);
        when(mockPresident.getEmail()).thenReturn("president@test.com");

        FormQuestion mockQuestion = mock(FormQuestion.class);
        when(mockQuestion.getId()).thenReturn(1L);
        when(mockQuestion.getDisplayOrder()).thenReturn(1L);
        when(mockQuestion.getFieldType()).thenReturn(FieldType.TIME_SLOT);
        when(mockQuestion.getIsRequired()).thenReturn(true);

        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(mockForm));
        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.of(mockUser));
        when(applicationRepository.findByStudentIdAndClubApplyForm(studentId, mockForm)).thenReturn(Optional.empty());
        when(clubMemberRepository.findUserByClubIdAndRoleAndStatus(clubId, Role.CLUB_ADMIN, ActiveStatus.ACTIVE))
                .thenReturn(Optional.of(mockPresident));
        when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(mockQuestion));

        // when
        ApplicationApplyResponseDto response = applicationService.submitApplication(clubId, requestDto, false);

        // then
        assertNotNull(response);
        
        // Application 저장 캡처
        ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(applicationCaptor.capture());
        Application savedApplication = applicationCaptor.getValue();

        // Answer 저장 캡처
        ArgumentCaptor<List<Answer>> answersCaptor = ArgumentCaptor.forClass(List.class);
        verify(answerRepository).saveAll(answersCaptor.capture());
        List<Answer> savedAnswers = answersCaptor.getValue();
        
        assertEquals(1, savedAnswers.size());
        // 저장된 답변 문자열 확인 (DateUtil.reassembleTimeSlots 로직에 따라 포맷팅됨)
        String savedAnswerStr = savedAnswers.get(0).getAnswer();
        assertTrue(savedAnswerStr.contains("2025-10-16 10:30-11:00"));
        assertTrue(savedAnswerStr.contains("2025-10-16 11:00-11:30"));

        // Application 객체 내 interviewPreferences 검증
        List<InterviewPreference> preferences = savedApplication.getInterviewPreferences();
        assertNotNull(preferences);
        assertEquals(1, preferences.size()); // 같은 날짜이므로 1개의 InterviewPreference 객체 생성 예상

        InterviewPreference pref = preferences.get(0);
        assertEquals(LocalDate.of(2025, 10, 16), pref.getDate());
        
        // 시간대 검증 (10:30, 11:00 시작 시간만 저장되는지 확인)
        List<LocalTime> times = pref.getTimes();
        assertEquals(2, times.size());
        assertTrue(times.contains(LocalTime.of(10, 30)));
        assertTrue(times.contains(LocalTime.of(11, 0)));
    }

    @Test
    @DisplayName("지원자 면접 일정 변경 - 성공")
    void updateApplicationInterviewSchedule_success() {
        // given
        Long applicationId = 1L;
        LocalDateTime newInterviewTime = LocalDateTime.of(2026, 2, 10, 10, 0);
        ApplicationFixedInterviewRequestDto requestDto = new ApplicationFixedInterviewRequestDto(newInterviewTime);

        Application mockApplication = mock(Application.class);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(mockApplication));

        // when
        SuccessResponseDto responseDto = applicationService.updateApplicationInterviewSchedule(applicationId, requestDto);

        // then
        assertTrue(responseDto.success());
        verify(applicationRepository, times(1)).findById(applicationId);
        verify(mockApplication, times(1)).updateInterviewInfo(newInterviewTime);
    }

    @Test
    @DisplayName("지원자 면접 일정 변경 - 실패 (지원서 없음)")
    void updateApplicationInterviewSchedule_fail_applicationNotFound() {
        // given
        Long nonExistentApplicationId = 999L;
        LocalDateTime newInterviewTime = LocalDateTime.of(2026, 2, 10, 10, 0);
        ApplicationFixedInterviewRequestDto requestDto = new ApplicationFixedInterviewRequestDto(newInterviewTime);

        when(applicationRepository.findById(nonExistentApplicationId)).thenReturn(Optional.empty());

        // when & then
        ApplicationNotFoundException exception = assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.updateApplicationInterviewSchedule(nonExistentApplicationId, requestDto);
        });

        assertEquals("applicationId: " + nonExistentApplicationId, exception.getDetail());
        verify(applicationRepository, times(1)).findById(nonExistentApplicationId);
    }
}
