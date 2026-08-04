package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.entity.ClubView;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.repository.ClubViewRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClubPopularityRecoveryServiceTest {

    @Mock ClubPopularityRedisRepository redisRepository;
    @Mock ClubViewRepository clubViewRepository;
    @Mock ClubView view;
    @Mock Club club;

    private ClubPopularityRecoveryService service;

    @BeforeEach
    void setUp() {
        service = new ClubPopularityRecoveryService(new ClubPopularityProperties(), redisRepository, clubViewRepository);
    }

    @Test
    void readyStatusSkipsRecovery() {
        when(redisRepository.recoveryStatus()).thenReturn("READY");

        assertThat(service.recoverIfNeeded()).isEqualTo(ClubPopularityRecoveryService.RecoveryResult.ALREADY_READY);
        verify(redisRepository, never()).tryAcquireRecoveryLock(any(), any());
    }

    @Test
    void rebuildsRecentViewersInBatchesAndActivatesReadyAfterSuccess() {
        when(redisRepository.recoveryStatus()).thenReturn(null);
        when(redisRepository.tryAcquireRecoveryLock(any(), any())).thenReturn(true);
        when(redisRepository.ownsRecoveryLock(any())).thenReturn(true);
        when(redisRepository.refreshRecoveryLock(any(), any())).thenReturn(true);
        when(clubViewRepository.findTop500ByIdGreaterThanAndLastViewedAtAfterOrderByIdAsc(anyLong(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0, Long.class) == 0L ? List.of(view) : List.of());
        when(view.getClub()).thenReturn(club);
        when(club.getId()).thenReturn(7L);
        when(view.redisMember()).thenReturn("U:15");
        when(view.getLastViewedAt()).thenReturn(Instant.ofEpochMilli(100));
        when(view.getId()).thenReturn(1L);

        assertThat(service.recoverIfNeeded()).isEqualTo(ClubPopularityRecoveryService.RecoveryResult.RECOVERED);
        InOrder order = inOrder(redisRepository);
        order.verify(redisRepository).setRecoveryStatus("RECOVERING");
        verify(redisRepository).clearRecentViewerKeys();
        verify(redisRepository).clearCandidates();
        verify(redisRepository).rebuildRecentViewer(7L, "U:15", 100L);
        verify(redisRepository).clearActiveViewerKeys();
        verify(redisRepository).setRecoveryStatus("READY");
        verify(redisRepository).releaseRecoveryLock(any());
    }

    @Test
    void lockCompetitionDoesNotChangeRecoveryState() {
        when(redisRepository.recoveryStatus()).thenReturn("RECOVERING");
        when(redisRepository.tryAcquireRecoveryLock(any(), any())).thenReturn(false);

        assertThat(service.recoverIfNeeded()).isEqualTo(ClubPopularityRecoveryService.RecoveryResult.LOCK_NOT_ACQUIRED);
        verify(redisRepository, never()).setRecoveryStatus(any());
    }
}
