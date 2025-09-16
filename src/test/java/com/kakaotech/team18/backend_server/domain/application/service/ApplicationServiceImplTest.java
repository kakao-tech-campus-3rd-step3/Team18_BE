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
import com.kakaotech.team18.backend_server.domain.Answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.Answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Test
    @DisplayName("지원서 상세 조회 - 성공")
    void getApplicationDetail_success() {
        // given
        Long clubId = 1L;
        Long applicantId = 1L;

        // Mock 객체 생성
        User mockUser = User.builder()
                .name("김지원")
                .department("컴퓨터공학과")
                .studentId("20230001")
                .email("test@test.com")
                .phoneNumber("010-1234-5678")
                .build();

        // 리플렉션을 사용해 ID 강제 주입
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockUser, applicantId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Application mockApplication = mock(Application.class);
        FormQuestion mockField1 = mock(FormQuestion.class);
        FormQuestion mockField2 = mock(FormQuestion.class);
        Answer mockAnswer1 = mock(Answer.class);
        Answer mockAnswer2 = mock(Answer.class);

        // Repository Mocking 설정
        when(applicationRepository.findByClub_IdAndUser_Id(clubId, applicantId)).thenReturn(Optional.of(mockApplication));
        when(answerRepository.findByApplicationWithFormField(mockApplication)).thenReturn(List.of(mockAnswer1, mockAnswer2));

        // Application Mocking 설정
        when(mockApplication.getId()).thenReturn(100L);
        when(mockApplication.getStatus()).thenReturn(Status.PENDING);
        when(mockApplication.getUser()).thenReturn(mockUser);

        // Answer 및 Field Mocking 설정
        when(mockAnswer1.getFormQuestionField()).thenReturn(mockField1);
        when(mockField1.getQuestion()).thenReturn("질문 1");
        when(mockAnswer1.getAnswer()).thenReturn("답변 1");

        when(mockAnswer2.getFormQuestionField()).thenReturn(mockField2);
        when(mockField2.getQuestion()).thenReturn("질문 2");
        when(mockAnswer2.getAnswer()).thenReturn("답변 2");

        // when
        ApplicationDetailResponseDto result = applicationService.getApplicationDetail(clubId, applicantId);

        // then
        assertNotNull(result);
        assertEquals(100L, result.applicationId());
        assertEquals("PENDING", result.status());
        assertEquals(applicantId, result.applicantInfo().applicantId());
        assertEquals("김지원", result.applicantInfo().name());
        assertEquals("컴퓨터공학과", result.applicantInfo().department());
        assertEquals(2, result.questionsAndAnswers().size());
        assertEquals("질문 1", result.questionsAndAnswers().get(0).question());
        assertEquals("답변 1", result.questionsAndAnswers().get(0).answer());

        // verify: 메소드가 정확히 1번씩 호출되었는지 검증
        verify(applicationRepository, times(1)).findByClub_IdAndUser_Id(clubId, applicantId);
        verify(answerRepository, times(1)).findByApplicationWithFormField(mockApplication);
    }

    @Test
    @DisplayName("지원서 상세 조회 - 실패 (지원서 없음)")
    void getApplicationDetail_fail_applicationNotFound() {
        // given
        Long clubId = 1L;
        Long nonExistentApplicantId = 999L;
        when(applicationRepository.findByClub_IdAndUser_Id(clubId, nonExistentApplicantId)).thenReturn(Optional.empty());

        // when & then
        ApplicationNotFoundException exception = assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.getApplicationDetail(clubId, nonExistentApplicantId);
        });
        
        assertEquals("clubId=1, applicantId=999", exception.getDetail());

        // verify
        verify(applicationRepository, times(1)).findByClub_IdAndUser_Id(clubId, nonExistentApplicantId);
        verify(answerRepository, never()).findByApplicationWithFormField(any(Application.class));
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
        // 첫 번째 getStatus() 호출(oldStatus 로깅)에는 PENDING, 두 번째 호출(newStatus 로깅)에는 APPROVED를 반환하도록 설정
        when(mockApplication.getStatus()).thenReturn(oldStatus, newStatus);

        // Logger 및 Appender 설정
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

        // Log 검증
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(2, logsList.size());
        assertEquals("지원서 상태 변경 시작: applicationId=1, oldStatus=PENDING, newStatus=APPROVED", logsList.get(0).getFormattedMessage());
        assertEquals("지원서 상태 변경 완료: applicationId=1, newStatus=APPROVED", logsList.get(1).getFormattedMessage());

        // Appender 정리
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
}
