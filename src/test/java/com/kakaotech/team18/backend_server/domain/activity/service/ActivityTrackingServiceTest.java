package com.kakaotech.team18.backend_server.domain.activity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.activity.entity.ActivityDaily;
import com.kakaotech.team18.backend_server.domain.activity.repository.ActivityDailyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ActivityTrackingServiceTest {

    @Mock
    private ActivityDailyRepository activityDailyRepository;

    private ActivityTrackingService activityTrackingService;

    @BeforeEach
    void setUp() {
        activityTrackingService = new ActivityTrackingService(activityDailyRepository);
    }

    @Test
    @DisplayName("익명 방문자 첫 활동은 hit_count=1로 저장된다")
    void track_anonymous_firstSeen() {
        String anonymousId = "anon-1";
        when(activityDailyRepository.findByActivityDateAndAnonymousId(any(LocalDate.class), any(String.class)))
                .thenReturn(Optional.empty());

        activityTrackingService.track(anonymousId, null);

        ArgumentCaptor<ActivityDaily> captor = ArgumentCaptor.forClass(ActivityDaily.class);
        verify(activityDailyRepository).save(captor.capture());
        ActivityDaily saved = captor.getValue();
        assertThat(saved.getAnonymousId()).isEqualTo(anonymousId);
        assertThat(saved.getUserId()).isNull();
        assertThat(saved.getHitCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인 사용자 기존 활동이 있으면 hit_count가 증가한다")
    void track_user_existingRow_touch() {
        Long userId = 10L;
        LocalDate today = LocalDate.now();
        ActivityDaily existing = ActivityDaily.forUser(today, userId, LocalDateTime.now().minusHours(1));
        existing.touch(LocalDateTime.now().minusMinutes(30)); // 현재 hit_count=2

        when(activityDailyRepository.findByActivityDateAndUserId(any(LocalDate.class), any(Long.class)))
                .thenReturn(Optional.of(existing));

        activityTrackingService.track("anon-ignored", userId);

        verify(activityDailyRepository).save(existing);
        assertThat(existing.getHitCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("익명 식별자가 없으면 저장하지 않는다")
    void track_missingAnonymousId_skip() {
        activityTrackingService.track("   ", null);

        verify(activityDailyRepository, never()).save(any(ActivityDaily.class));
    }
}
