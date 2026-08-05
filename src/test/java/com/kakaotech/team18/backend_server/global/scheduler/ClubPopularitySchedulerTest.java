package com.kakaotech.team18.backend_server.global.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityPersistenceService;
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

    @Test
    void flushesUntilTheConfiguredRunLimitOrQueueIsEmpty() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getFlushBatchSize()).thenReturn(2);
        when(properties.getFlushMaxRecordsPerRun()).thenReturn(5);
        when(properties.getFlushMaxDbAttemptsPerRun()).thenReturn(3);
        when(properties.getFlushMaxDurationMinutes()).thenReturn(1);
        when(persistenceService.flushPending(2)).thenReturn(
                new ClubPopularityPersistenceService.FlushResult(2, 2, 0, 0, false),
                new ClubPopularityPersistenceService.FlushResult(2, 2, 0, 0, false));
        when(persistenceService.flushPending(1)).thenReturn(
                new ClubPopularityPersistenceService.FlushResult(0, 0, 0, 0, false));

        ClubPopularityScheduler scheduler = new ClubPopularityScheduler(properties, persistenceService);

        scheduler.flushPending();

        verify(persistenceService, times(2)).flushPending(2);
        verify(persistenceService).flushPending(1);
        verify(persistenceService).retryFailedRecords(2);
    }
}
