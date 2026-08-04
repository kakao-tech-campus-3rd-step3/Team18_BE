package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.entity.ClubView;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.repository.ClubViewRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.metrics.ClubPopularityMetrics;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubPopularityRecoveryService {

    private static final String READY = "READY";
    private static final String RECOVERING = "RECOVERING";
    private static final int BATCH_SIZE = 500;

    private final ClubPopularityProperties properties;
    private final ClubPopularityRedisRepository redisRepository;
    private final ClubViewRepository clubViewRepository;
    private final ClubPopularityMetrics metrics;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverOnStartup() {
        recoverIfNeeded();
    }

    @Scheduled(fixedDelayString = "${club-popularity.recovery-check-interval-minutes:5}000")
    public void recoverOnSchedule() {
        recoverIfNeeded();
    }

    public RecoveryResult recoverIfNeeded() {
        if (!properties.isEnabled()) {
            metrics.setEnabled(false);
            return RecoveryResult.DISABLED;
        }
        long startedAt = System.currentTimeMillis();
        try {
            if (READY.equals(redisRepository.recoveryStatus())) {
                return RecoveryResult.ALREADY_READY;
            }
            String owner = UUID.randomUUID().toString();
            Duration lockTtl = Duration.ofMinutes(properties.getRecoveryMaxDurationMinutes());
            if (!redisRepository.tryAcquireRecoveryLock(owner, lockTtl)) {
                return RecoveryResult.LOCK_NOT_ACQUIRED;
            }
            metrics.setRecoveryInProgress(true);
            try {
                redisRepository.setRecoveryStatus(RECOVERING);
                redisRepository.clearRecentViewerKeys();
                redisRepository.clearCandidates();
                Instant cutoff = Instant.now().minusSeconds(24 * 60 * 60);
                Instant deadline = Instant.now().plus(lockTtl);
                long lastId = 0L;
                while (true) {
                    if (Instant.now().isAfter(deadline)) {
                        log.error("Club popularity recovery exceeded configured maximum duration");
                        return RecoveryResult.TIMEOUT;
                    }
                    if (!redisRepository.ownsRecoveryLock(owner) || !redisRepository.refreshRecoveryLock(owner, lockTtl)) {
                        log.warn("Club popularity recovery lock ownership lost");
                        return RecoveryResult.LOCK_LOST;
                    }
                    List<ClubView> batch = clubViewRepository
                            .findTop500ByIdGreaterThanAndLastViewedAtAfterOrderByIdAsc(lastId, cutoff);
                    for (ClubView view : batch) {
                        redisRepository.rebuildRecentViewer(view.getClub().getId(), view.redisMember(),
                                view.getLastViewedAt().toEpochMilli());
                        lastId = view.getId();
                    }
                    if (batch.size() < BATCH_SIZE) {
                        break;
                    }
                }
                if (!redisRepository.ownsRecoveryLock(owner)) {
                    return RecoveryResult.LOCK_LOST;
                }
                redisRepository.clearActiveViewerKeys();
                redisRepository.setRecoveryStatus(READY);
                metrics.recordRecovery("recovered", (System.currentTimeMillis() - startedAt) / 1000);
                return RecoveryResult.RECOVERED;
            } finally {
                metrics.setRecoveryInProgress(false);
                redisRepository.releaseRecoveryLock(owner);
            }
        } catch (DataAccessException exception) {
            metrics.recordRedisError("recovery");
            metrics.recordRecovery("redis_error", (System.currentTimeMillis() - startedAt) / 1000);
            metrics.setRecoveryInProgress(false);
            log.warn("Club popularity recovery deferred because Redis is unavailable: {}", exception.getMessage());
            return RecoveryResult.REDIS_UNAVAILABLE;
        } catch (RuntimeException exception) {
            metrics.recordRecovery("failed", (System.currentTimeMillis() - startedAt) / 1000);
            metrics.setRecoveryInProgress(false);
            log.error("Club popularity recovery failed; keeping RECOVERING status", exception);
            return RecoveryResult.FAILED;
        }
    }

    public enum RecoveryResult {
        RECOVERED,
        ALREADY_READY,
        LOCK_NOT_ACQUIRED,
        LOCK_LOST,
        REDIS_UNAVAILABLE,
        DISABLED,
        TIMEOUT,
        FAILED
    }
}
