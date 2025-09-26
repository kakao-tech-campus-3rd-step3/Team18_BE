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
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
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

    @Mock
    private ClubApplyFormRepository clubApplyFormRepository;

    @Test
    @DisplayName("지원서 상세 조회 - 성공 (여러 지원서 중 특정 지원서 조회)")
    void getApplicationDetail_success_whenMultipleApplicationsExist() {
        // given
        Long clubId = 1L;
        Long userId = 1L;

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
        when(mockClubApplyForm.getId()).thenReturn(mockFormId);
        when(applicationRepository.findByClubApplyFormIdAndUserId(mockFormId, userId)).thenReturn(Optional.of(mockApplication));

        when(answerRepository.findByApplicationWithFormQuestion(mockApplication)).thenReturn(List.of(mockAnswer));
        when(mockApplication.getId()).thenReturn(100L);
        when(mockApplication.getStatus()).thenReturn(Status.PENDING);
        when(mockApplication.getUser()).thenReturn(mockUser);
        when(mockAnswer.getFormQuestion()).thenReturn(mockQuestion);
        when(mockQuestion.getQuestion()).thenReturn("리팩토링된 질문");
        when(mockAnswer.getAnswer()).thenReturn("리팩토링된 답변");

        // when
        ApplicationDetailResponseDto result = applicationService.getApplicationDetail(clubId, userId);

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
        verify(applicationRepository, times(1)).findByClubApplyFormIdAndUserId(mockFormId, userId);
        verify(answerRepository, times(1)).findByApplicationWithFormQuestion(mockApplication);
    }

    @Test
    @DisplayName("지원서 상세 조회 - 실패 (지원서는 존재하지 않음)")
    void getApplicationDetail_fail_applicationNotFound() {
        // given
        Long clubId = 1L;
        Long userId = 999L;
        Long formId = 10L;

        ClubApplyForm mockClubApplyForm = mock(ClubApplyForm.class);
        when(clubApplyFormRepository.findByClubId(clubId)).thenReturn(Optional.of(mockClubApplyForm));
        when(mockClubApplyForm.getId()).thenReturn(formId);

        when(applicationRepository.findByClubApplyFormIdAndUserId(formId, userId)).thenReturn(Optional.empty());

        // when & then
        ApplicationNotFoundException exception = assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.getApplicationDetail(clubId, userId);
        });

        assertEquals("해당 지원서를 찾을 수 없습니다.", exception.getMessage());

        // verify
        verify(clubApplyFormRepository, times(1)).findByClubId(clubId);
        verify(applicationRepository, times(1)).findByClubApplyFormIdAndUserId(formId, userId);
        verify(answerRepository, never()).findByApplicationWithFormQuestion(any(Application.class));
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
}
