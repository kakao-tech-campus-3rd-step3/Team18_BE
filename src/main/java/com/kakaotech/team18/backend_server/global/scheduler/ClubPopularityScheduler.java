package com.kakaotech.team18.backend_server.global.scheduler;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityPersistenceService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubPopularityScheduler {

    private final ClubPopularityProperties properties;
    private final ClubPopularityPersistenceService persistenceService;

    @Scheduled(fixedDelayString = "${club-popularity.flush-interval-minutes:60}000")
    public void flushPending() {
        if (!properties.isEnabled()) {
            return;
        }
        log.info("Starting club popularity pending flush");
        ClubPopularityPersistenceService.FlushResult result = persistenceService.flushPending(
                properties.getFlushBatchSize());
        log.info("Club popularity pending flush completed: {}", result);
    }

    @Scheduled(fixedDelayString = "${club-popularity.cleanup-interval-minutes:60}000")
    public void cleanupOldViews() {
        if (!properties.isEnabled()) {
            return;
        }
        Instant cutoff = Instant.now().minusSeconds((long) properties.getRetentionHours() * 3600);
        int deleted = persistenceService.cleanupOldViews(cutoff, properties.getCleanupBatchSize());
        log.info("Club popularity old view cleanup completed: {} rows", deleted);
    }
}
