package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyLong;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityTimePolicy;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.metrics.ClubPopularityMetrics;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ClubPopularityRecordingServiceTest {

    @Mock
    private ClubPopularityTimePolicy timePolicy;

    @Mock
    private ClubPopularityViewerResolver viewerResolver;

    @Mock
    private ClubPopularityRedisRepository redisRepository;

    @Mock
    private ClubPopularityMetrics metrics;

    @Mock
    private Authentication authentication;

    private ClubPopularityProperties properties;
    private ClubPopularityRecordingService service;

    @BeforeEach
    void setUp() {
        properties = new ClubPopularityProperties();
        service = new ClubPopularityRecordingService(properties, timePolicy, viewerResolver, redisRepository, metrics);
        lenient().when(timePolicy.currentInstant()).thenReturn(Instant.parse("2026-08-02T00:00:00Z"));
    }

    @Test
    @DisplayName("기록 성공 결과를 204용 내부 결과로 변환한다")
    void recordsView() {
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.user(15L);
        when(viewerResolver.resolve(authentication, "anonymous-id")).thenReturn(Optional.of(identity));
        when(redisRepository.recordView(anyLong(), any(), anyLong(), any(Integer.class), any(Integer.class),
                anyLong())).thenReturn(ClubPopularityRedisRepository.RecordResult.RECORDED);

        assertThat(service.recordView(7, authentication, "anonymous-id"))
                .isEqualTo(ClubPopularityRecordingService.RecordingResult.RECORDED);
        verify(redisRepository).recordView(7, identity, 1_785_628_800_000L, 5, 180, 90_000L);
    }

    @Test
    @DisplayName("기능이 비활성화되면 사용자 식별과 Redis 호출을 생략한다")
    void skipsWhenDisabled() {
        properties.setEnabled(false);

        assertThat(service.recordHeartbeat(7, authentication, "anonymous-id"))
                .isEqualTo(ClubPopularityRecordingService.RecordingResult.DISABLED);
        verifyNoResolverOrRepositoryInteraction();
    }

    @Test
    @DisplayName("식별할 수 없는 사용자는 Redis에 기록하지 않는다")
    void skipsInvalidIdentity() {
        when(viewerResolver.resolve(authentication, null)).thenReturn(Optional.empty());

        assertThat(service.recordView(7, authentication, null))
                .isEqualTo(ClubPopularityRecordingService.RecordingResult.INVALID_IDENTITY);
        verify(redisRepository, never()).recordView(anyLong(), any(), anyLong(), any(Integer.class),
                any(Integer.class), anyLong());
    }

    @Test
    @DisplayName("Redis 장애는 사용자 요청을 실패시키지 않고 Redis 오류 결과로 격리한다")
    void isolatesRedisFailure() {
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.anonymous("anonymous-id");
        when(viewerResolver.resolve(null, "anonymous-id")).thenReturn(Optional.of(identity));
        when(redisRepository.recordHeartbeat(any(Long.class), any(), any(Long.class), any(Integer.class),
                any(Integer.class))).thenThrow(new DataAccessResourceFailureException("redis down"));

        assertThat(service.recordHeartbeat(7, null, "anonymous-id"))
                .isEqualTo(ClubPopularityRecordingService.RecordingResult.REDIS_ERROR);
    }

    @Test
    @DisplayName("Redis의 요청 제한과 복구 중 결과를 보존한다")
    void mapsRedisStatuses() {
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.user(15L);
        when(viewerResolver.resolve(authentication, null)).thenReturn(Optional.of(identity));
        when(redisRepository.recordView(anyLong(), any(), anyLong(), any(Integer.class), any(Integer.class),
                anyLong())).thenReturn(ClubPopularityRedisRepository.RecordResult.RATE_LIMITED,
                ClubPopularityRedisRepository.RecordResult.RECOVERING);

        assertThat(service.recordView(7, authentication, null))
                .isEqualTo(ClubPopularityRecordingService.RecordingResult.RATE_LIMITED);
        assertThat(service.recordView(7, authentication, null))
                .isEqualTo(ClubPopularityRecordingService.RecordingResult.RECOVERING);
    }

    @Test
    void mapsUnknownClubResultWithoutChangingTheHttpContract() {
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.user(15L);
        when(viewerResolver.resolve(authentication, null)).thenReturn(Optional.of(identity));
        when(redisRepository.recordView(anyLong(), any(), anyLong(), any(Integer.class), any(Integer.class), anyLong()))
                .thenReturn(ClubPopularityRedisRepository.RecordResult.INVALID_CLUB);

        assertThat(service.recordView(999, authentication, null))
                .isEqualTo(ClubPopularityRecordingService.RecordingResult.INVALID_CLUB);
    }

    private void verifyNoResolverOrRepositoryInteraction() {
        verify(viewerResolver, never()).resolve(any(), any());
        verify(redisRepository, never()).recordView(anyLong(), any(), anyLong(), any(Integer.class),
                any(Integer.class), anyLong());
        verify(redisRepository, never()).recordHeartbeat(any(Long.class), any(), any(Long.class), any(Integer.class),
                any(Integer.class));
    }
}
