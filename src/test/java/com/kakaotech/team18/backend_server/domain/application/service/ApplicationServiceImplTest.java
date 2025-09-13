package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.applicationFormAnswer.entity.ApplicationFormAnswer;
import com.kakaotech.team18.backend_server.domain.applicationFormAnswer.repository.ApplicationFormAnswerRepository;
import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationFormAnswerRepository applicationFormAnswerRepository;

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
        ApplicationFormField mockField1 = mock(ApplicationFormField.class);
        ApplicationFormField mockField2 = mock(ApplicationFormField.class);
        ApplicationFormAnswer mockAnswer1 = mock(ApplicationFormAnswer.class);
        ApplicationFormAnswer mockAnswer2 = mock(ApplicationFormAnswer.class);

        // Repository Mocking 설정
        when(applicationRepository.findByClub_IdAndUser_Id(clubId, applicantId)).thenReturn(Optional.of(mockApplication));
        when(applicationFormAnswerRepository.findByApplicationWithFormField(mockApplication)).thenReturn(List.of(mockAnswer1, mockAnswer2));

        // Application Mocking 설정
        when(mockApplication.getId()).thenReturn(100L);
        when(mockApplication.getStatus()).thenReturn(Status.PENDING);
        when(mockApplication.getUser()).thenReturn(mockUser);

        // Answer 및 Field Mocking 설정
        when(mockAnswer1.getApplicationFormField()).thenReturn(mockField1);
        when(mockField1.getQuestion()).thenReturn("질문 1");
        when(mockAnswer1.getAnswer()).thenReturn("답변 1");

        when(mockAnswer2.getApplicationFormField()).thenReturn(mockField2);
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
        verify(applicationFormAnswerRepository, times(1)).findByApplicationWithFormField(mockApplication);
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
        verify(applicationFormAnswerRepository, never()).findByApplicationWithFormField(any(Application.class));
    }

    @Test
    @DisplayName("지원서 상태 변경 - 성공")
    void updateApplicationStatus_success() {
        // given
        Long applicationId = 1L;
        Status newStatus = Status.APPROVED;
        ApplicationStatusUpdateRequestDto requestDto = new ApplicationStatusUpdateRequestDto(newStatus);

        Application mockApplication = mock(Application.class);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(mockApplication));

        // when
        SuccessResponseDto responseDto = applicationService.updateApplicationStatus(applicationId, requestDto);

        // then
        assertTrue(responseDto.success());
        verify(applicationRepository, times(1)).findById(applicationId);
        verify(mockApplication, times(1)).updateStatus(newStatus); // 엔티티의 상태 변경 메소드가 호출되었는지 검증
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
