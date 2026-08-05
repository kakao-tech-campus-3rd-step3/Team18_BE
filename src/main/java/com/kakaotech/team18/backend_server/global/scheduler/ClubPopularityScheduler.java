package com.kakaotech.team18.backend_server.global.scheduler;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityPersistenceService;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
        ClubPopularityPersistenceService.FlushResult result = flushWithinLimits();
        log.info("Club popularity pending flush completed: {}", result);
        persistenceService.retryFailedRecords(properties.getFlushBatchSize());
    }

    private ClubPopularityPersistenceService.FlushResult flushWithinLimits() {
        long deadline = System.nanoTime()
                + Duration.ofMinutes(properties.getFlushMaxDurationMinutes()).toNanos();
        int processed = 0;
        int attempts = 0;
        ClubPopularityPersistenceService.FlushResult last = new ClubPopularityPersistenceService.FlushResult(
                0, 0, 0, 0, false);
        while (processed < properties.getFlushMaxRecordsPerRun()
                && attempts < properties.getFlushMaxDbAttemptsPerRun()
                && System.nanoTime() < deadline) {
            int batchSize = Math.min(properties.getFlushBatchSize(),
                    properties.getFlushMaxRecordsPerRun() - processed);
            try {
                attempts++;
                last = persistenceService.flushPending(batchSize);
                if (last.skipped() || last.removed() == 0) {
                    break;
                }
                processed += last.removed();
            } catch (DataAccessException exception) {
                log.warn("Club popularity pending flush attempt {}/{} failed: {}",
                        attempts, properties.getFlushMaxDbAttemptsPerRun(), exception.getMessage());
                if (attempts >= properties.getFlushMaxDbAttemptsPerRun()) {
                    break;
                }
                awaitTransientRetry(attempts, deadline);
            }
        }
        return last;
    }

    private void awaitTransientRetry(int attempt, long deadline) {
        List<Integer> delays = properties.getTransientRetryDelaysSeconds();
        int delaySeconds = delays.get(Math.min(attempt - 1, delays.size() - 1));
        long remainingNanos = deadline - System.nanoTime();
        long sleepMillis = Math.min(delaySeconds * 1_000L,
                Math.max(0L, Duration.ofNanos(remainingNanos).toMillis()));
        if (sleepMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
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
