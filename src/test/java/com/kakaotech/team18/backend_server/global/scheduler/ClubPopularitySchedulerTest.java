package com.kakaotech.team18.backend_server.global.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityPersistenceService;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClubPopularitySchedulerTest {

    @Mock
    private ClubPopularityProperties properties;

    @Mock
    private ClubPopularityPersistenceService persistenceService;

    @Mock
    private ClubPopularityRedisRepository redisRepository;

    @Test
    void flushesUntilTheConfiguredRunLimitOrQueueIsEmpty() {
        when(properties.isEnabled()).thenReturn(true);
        when(redisRepository.recoveryStatus()).thenReturn("READY");
        when(properties.getFlushBatchSize()).thenReturn(2);
        when(properties.getFlushMaxRecordsPerRun()).thenReturn(5);
        when(properties.getFlushMaxDbAttemptsPerRun()).thenReturn(3);
        when(properties.getFlushMaxDurationMinutes()).thenReturn(1);
        when(persistenceService.flushPending(2)).thenReturn(
                new ClubPopularityPersistenceService.FlushResult(2, 2, 0, 0, false),
                new ClubPopularityPersistenceService.FlushResult(2, 2, 0, 0, false));
        when(persistenceService.flushPending(1)).thenReturn(
                new ClubPopularityPersistenceService.FlushResult(0, 0, 0, 0, false));

        ClubPopularityScheduler scheduler = new ClubPopularityScheduler(properties, persistenceService, redisRepository);

        scheduler.flushPending();

        verify(persistenceService, times(2)).flushPending(2);
        verify(persistenceService).flushPending(1);
        verify(persistenceService).retryFailedRecords(2);
    }

    @Test
    void continuesWhenBatchContainsOnlyIsolatedFailures() {
        when(properties.isEnabled()).thenReturn(true);
        when(redisRepository.recoveryStatus()).thenReturn("READY");
        when(properties.getFlushBatchSize()).thenReturn(2);
        when(properties.getFlushMaxRecordsPerRun()).thenReturn(4);
        when(properties.getFlushMaxDbAttemptsPerRun()).thenReturn(2);
        when(properties.getFlushMaxDurationMinutes()).thenReturn(1);
        when(persistenceService.flushPending(2)).thenReturn(
                new ClubPopularityPersistenceService.FlushResult(0, 2, 0, 2, false),
                new ClubPopularityPersistenceService.FlushResult(0, 0, 0, 0, false));

        ClubPopularityScheduler scheduler = new ClubPopularityScheduler(properties, persistenceService, redisRepository);

        scheduler.flushPending();

        verify(persistenceService, times(2)).flushPending(2);
        verify(persistenceService).retryFailedRecords(2);
    }

    @Test
    void skipsFlushAndRetryWhileRecoveryIsInProgress() {
        when(properties.isEnabled()).thenReturn(true);
        when(redisRepository.recoveryStatus()).thenReturn("RECOVERING");

        new ClubPopularityScheduler(properties, persistenceService, redisRepository).flushPending();

        verify(persistenceService, org.mockito.Mockito.never()).flushPending(org.mockito.ArgumentMatchers.anyInt());
        verify(persistenceService, org.mockito.Mockito.never()).retryFailedRecords(org.mockito.ArgumentMatchers.anyInt());
    }
}
