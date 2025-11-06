package com.kakaotech.team18.backend_server.domain.comment.service;

import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.TemporaryServerConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class CommentServiceIntegrationTest {

    @Autowired
    private CommentServiceImpl commentService;

    @MockBean
    private ApplicationRepository applicationRepository;

    @DisplayName("평균점수 업데이트 실패 - 락 경합으로 재시도 후 실패")
    @Test
    void updateAverageRating_fail_afterRetries_thenRecover() {
        // given
        final Long applicationId = 1L;
        final String expectedExceptionDetail = "DB Lock failed";

        given(applicationRepository.findByIdWithPessimisticLock(anyLong()))
                .willThrow(new PessimisticLockingFailureException(expectedExceptionDetail));

        // when & then
        // 재시도가 모두 실패하고 @Recover 메소드가 던진 예외가 최종적으로 발생하는지 검증
        TemporaryServerConflictException exception = assertThrows(TemporaryServerConflictException.class, () -> {
            commentService.updateApplicationAverageRating(applicationId);
        });

        // then
        // CustomException의 detail 필드에 원인 메시지가 잘 담겼는지 확인
        assertThat(exception.getDetail()).isEqualTo(expectedExceptionDetail);

        // @Retryable(maxAttempts=3) 설정에 따라, findByIdWithPessimisticLock 메소드가 3번 호출되었는지 검증
        verify(applicationRepository, times(3)).findByIdWithPessimisticLock(applicationId);
    }
}
