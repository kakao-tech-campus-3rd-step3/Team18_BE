package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApprovedRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.notification.entity.ResultNotificationRequest;
import com.kakaotech.team18.backend_server.domain.notification.repository.ResultNotificationRequestRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.ResultNotificationDeliveryService;
import com.kakaotech.team18.backend_server.domain.notification.util.NotificationRequestFingerprintGenerator;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.IdempotencyKeyConflictException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidIdempotencyKeyException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.TemporaryServerConflictException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotAcquireLockException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceIdempotencyTest {

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private ClubApplyFormRepository clubApplyFormRepository;
    @Mock
    private FormQuestionRepository formQuestionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private ClubMemberRepository clubMemberRepository;
    @Mock
    private ResultNotificationRequestRepository resultNotificationRequestRepository;
    @Mock
    private ResultNotificationDeliveryService resultNotificationDeliveryService;

    @Test
    @DisplayName("동일한 멱등성 키와 동일한 요청은 기존 성공 결과를 반환한다")
    void returnExistingResultForSameRequest() {
        Long clubId = 1L;
        String idempotencyKey = "same-request-key";
        ApplicationApprovedRequestDto request = new ApplicationApprovedRequestDto("결과 안내");
        ClubApplyForm form = mock(ClubApplyForm.class);
        String fingerprint = NotificationRequestFingerprintGenerator.generate(clubId, Stage.INTERVIEW, request);
        ResultNotificationRequest existing = ResultNotificationRequest.processing(
                clubId,
                idempotencyKey,
                fingerprint,
                Stage.INTERVIEW
        );
        existing.complete(true);

        when(clubApplyFormRepository.findByClubIdForUpdate(clubId)).thenReturn(Optional.of(form));
        when(resultNotificationRequestRepository.findByClubIdAndIdempotencyKey(clubId, idempotencyKey))
                .thenReturn(Optional.of(existing));

        SuccessResponseDto response = applicationService.sendPassFailMessage(
                clubId,
                request,
                Stage.INTERVIEW,
                idempotencyKey
        );

        assertThat(response.success()).isTrue();
        verify(resultNotificationRequestRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(applicationRepository, publisher, clubMemberRepository);
    }

    @Test
    @DisplayName("동일한 멱등성 키에 다른 요청이 들어오면 409 예외를 발생시킨다")
    void rejectDifferentRequestWithSameKey() {
        Long clubId = 1L;
        String idempotencyKey = "conflict-key";
        ApplicationApprovedRequestDto request = new ApplicationApprovedRequestDto("변경된 결과 안내");
        ResultNotificationRequest existing = ResultNotificationRequest.processing(
                clubId,
                idempotencyKey,
                "different-fingerprint",
                Stage.INTERVIEW
        );

        when(clubApplyFormRepository.findByClubIdForUpdate(clubId))
                .thenReturn(Optional.of(mock(ClubApplyForm.class)));
        when(resultNotificationRequestRepository.findByClubIdAndIdempotencyKey(clubId, idempotencyKey))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> applicationService.sendPassFailMessage(
                clubId,
                request,
                Stage.INTERVIEW,
                idempotencyKey
        )).isInstanceOf(IdempotencyKeyConflictException.class);

        verify(resultNotificationRequestRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(applicationRepository, publisher, clubMemberRepository);
    }

    @Test
    @DisplayName("공백 또는 100자를 초과한 멱등성 키는 거절한다")
    void rejectInvalidIdempotencyKey() {
        ApplicationApprovedRequestDto request = new ApplicationApprovedRequestDto("결과 안내");

        assertThatThrownBy(() -> applicationService.sendPassFailMessage(1L, request, Stage.FINAL, " "))
                .isInstanceOf(InvalidIdempotencyKeyException.class);
        assertThatThrownBy(() -> applicationService.sendPassFailMessage(
                1L,
                request,
                Stage.FINAL,
                "a".repeat(101)
        )).isInstanceOf(InvalidIdempotencyKeyException.class);

        verifyNoInteractions(clubApplyFormRepository, resultNotificationRequestRepository);
    }

    @Test
    @DisplayName("동시 요청의 잠금을 얻지 못하면 재시도 가능한 409 예외를 발생시킨다")
    void translateLockFailureToTemporaryConflict() {
        ApplicationApprovedRequestDto request = new ApplicationApprovedRequestDto("결과 안내");
        when(clubApplyFormRepository.findByClubIdForUpdate(1L))
                .thenThrow(new CannotAcquireLockException("lock timeout"));

        assertThatThrownBy(() -> applicationService.sendPassFailMessage(
                1L,
                request,
                Stage.FINAL,
                "lock-conflict-key"
        )).isInstanceOf(TemporaryServerConflictException.class);

        verifyNoInteractions(resultNotificationRequestRepository);
    }
}
