package com.kakaotech.team18.backend_server.global.scheduler;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityPersistenceService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubPopularityScheduler {

    private final ClubPopularityProperties properties;
    private final ClubPopularityPersistenceService persistenceService;

    @Scheduled(fixedDelayString = "${club-popularity.flush-interval-minutes:60}", timeUnit = TimeUnit.MINUTES)
    public void flushPending() {
        if (!properties.isEnabled()) {
            return;
        }
        log.info("Starting club popularity pending flush");
        ClubPopularityPersistenceService.FlushResult result = persistenceService.flushPending(
                properties.getFlushBatchSize());
        log.info("Club popularity pending flush completed: {}", result);
        persistenceService.retryFailedRecords(properties.getFlushBatchSize());
    }

    @Scheduled(fixedDelayString = "${club-popularity.cleanup-interval-minutes:60}", timeUnit = TimeUnit.MINUTES)
    public void cleanupOldViews() {
        if (!properties.isEnabled()) {
            return;
        }
        Instant cutoff = Instant.now().minusSeconds((long) properties.getRetentionHours() * 3600);
        int deleted = 0;
        int batchSize = properties.getCleanupBatchSize();
        int maxIterations = Math.max(1, properties.getFlushMaxRecordsPerRun() / batchSize);
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int batchDeleted = persistenceService.cleanupOldViews(cutoff, batchSize);
            deleted += batchDeleted;
            if (batchDeleted < batchSize) {
                break;
            }
        }
        persistenceService.cleanupExpiredFailures(cutoff, batchSize);
        log.info("Club popularity old view cleanup completed: {} rows", deleted);
    }
}
