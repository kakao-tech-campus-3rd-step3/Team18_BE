package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.metrics.ClubPopularityMetrics;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityPendingKey;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisKeys;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.repository.ClubViewBatchRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubPopularityPersistenceService {

    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end",
            Long.class);

    private final ClubPopularityRedisRepository redisRepository;
    @Qualifier("clubViewRepository")
    private final ClubViewBatchRepository clubViewRepository;
    private final ClubRepository clubRepository;
    private final StringRedisTemplate redisTemplate;
    private final ClubPopularityProperties properties;
    private final ClubPopularityMetrics metrics;

    /** 한 번에 처리할 양을 제한해 DB 잠금과 Redis 왕복을 bounded하게 유지한다. */
    @Transactional
    public FlushResult flushPending(int batchSize) {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(ClubPopularityRedisKeys.FLUSH_LOCK, lockValue, java.time.Duration.ofMinutes(15));
        if (!Boolean.TRUE.equals(locked)) {
            return FlushResult.lockSkipped();
        }
        try {
            return flushLocked(batchSize);
        } finally {
            redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(ClubPopularityRedisKeys.FLUSH_LOCK), lockValue);
        }
    }

    @Transactional
    public FlushResult flushLocked(int batchSize) {
        io.micrometer.core.instrument.Timer.Sample timer = metrics.startDbTimer();
        refreshQueueMetrics();
        List<ClubPopularityRedisRepository.PendingRecord> records = redisRepository.pendingRecords(batchSize);
        if (records.isEmpty()) {
            metrics.stopDbTimer(timer);
            return new FlushResult(0, 0, 0, 0, false);
        }

        Map<Long, Club> clubs = loadClubs(records);
        List<ClubPopularityRedisRepository.PendingRecord> pendingRemovals = new ArrayList<>();
        int saved = 0;
        int removed = 0;
        int missing = 0;
        for (ClubPopularityRedisRepository.PendingRecord record : records) {
            ClubPopularityPendingKey.Parsed parsed;
            try {
                parsed = ClubPopularityPendingKey.parse(record.member());
            } catch (IllegalArgumentException exception) {
                isolateFailure(record, exception.getMessage());
                pendingRemovals.add(record);
                continue;
            }
            if (!clubs.containsKey(parsed.clubId())) {
                missing++;
                pendingRemovals.add(record);
                removed++;
                continue;
            }
            Instant viewedAt = Instant.ofEpochMilli(record.scoreMillis());
            try {
                if ("U".equals(parsed.type())) {
                    clubViewRepository.upsertUser(parsed.clubId(), Long.parseLong(parsed.value()), viewedAt);
                } else {
                    clubViewRepository.upsertAnonymous(parsed.clubId(),
                            Base64.getUrlDecoder().decode(parsed.value()), viewedAt);
                }
            } catch (DataIntegrityViolationException exception) {
                isolateFailure(record, exception.getMessage());
                pendingRemovals.add(record);
                continue;
            }
            saved++;
            pendingRemovals.add(record);
            removed++;
        }
        registerPendingRemovals(pendingRemovals);
        metrics.recordDbSaved(saved);
        metrics.recordDbMissing(missing);
        refreshQueueMetrics();
        metrics.stopDbTimer(timer);
        return new FlushResult(saved, removed, missing, 0, false);
    }

    private void refreshQueueMetrics() {
        metrics.setPendingState(redisRepository.pendingCount(),
                redisRepository.oldestPendingAgeSeconds(System.currentTimeMillis()));
        metrics.setFailedCount(redisRepository.failedRecordCount(System.currentTimeMillis()));
    }

    private void isolateFailure(ClubPopularityRedisRepository.PendingRecord record, String reason) {
        String failureId = UUID.randomUUID().toString();
        int firstRetryDelayMinutes = properties.getFailedRecordRetryDelaysMinutes().get(0);
        redisRepository.saveFailedRecord(failureId, record, reason == null ? "invalid record" : reason,
                System.currentTimeMillis() + firstRetryDelayMinutes * 60_000L,
                java.time.Duration.ofHours(properties.getFailedRecordRetentionHours()));
        log.warn("Club popularity pending record moved to failed store: failureId={}", failureId);
    }

    @Transactional
    public int cleanupOldViews(Instant cutoff, int batchSize) {
        return clubViewRepository.deleteOlderThan(cutoff, batchSize);
    }

    @Transactional
    public int retryFailedRecords(int limit) {
        int processed = 0;
        for (String failureId : redisRepository.dueFailedRecords(System.currentTimeMillis(), limit)) {
            Map<Object, Object> data = redisRepository.failedRecord(failureId);
            if (data.isEmpty()) {
                redisRepository.removeFailedRecord(failureId);
                processed++;
                continue;
            }
            try {
                ClubPopularityRedisRepository.PendingRecord record = new ClubPopularityRedisRepository.PendingRecord(
                        (String) data.get("member"), Long.parseLong((String) data.get("scoreMillis")));
                ClubPopularityPendingKey.Parsed parsed = ClubPopularityPendingKey.parse(record.member());
                if (clubRepository.findById(parsed.clubId()).isEmpty()) {
                    redisRepository.removeFailedRecord(failureId);
                    processed++;
                    continue;
                }
                Instant viewedAt = Instant.ofEpochMilli(record.scoreMillis());
                if ("U".equals(parsed.type())) {
                    clubViewRepository.upsertUser(parsed.clubId(), Long.parseLong(parsed.value()), viewedAt);
                } else {
                    clubViewRepository.upsertAnonymous(parsed.clubId(), Base64.getUrlDecoder().decode(parsed.value()), viewedAt);
                }
                redisRepository.removeFailedRecord(failureId);
            } catch (RuntimeException exception) {
                int attempt = parseAttempt(data.get("attempt"));
                if (attempt >= properties.getFailedRecordMaxAttempts()) {
                    redisRepository.removeFailedRecord(failureId);
                } else {
                    List<Integer> delays = properties.getFailedRecordRetryDelaysMinutes();
                    int delayIndex = attempt - 1;
                    if (delayIndex < 0 || delayIndex >= delays.size()) {
                        redisRepository.removeFailedRecord(failureId);
                        processed++;
                        continue;
                    }
                    int delayMinutes = delays.get(delayIndex);
                    redisRepository.rescheduleFailedRecord(failureId, attempt + 1,
                            System.currentTimeMillis() + delayMinutes * 60_000L,
                            java.time.Duration.ofHours(properties.getFailedRecordRetentionHours()));
                }
            }
            processed++;
        }
        return processed;
    }

    private int parseAttempt(Object value) {
        try {
            return Integer.parseInt(String.valueOf(value == null ? "1" : value));
        } catch (NumberFormatException exception) {
            return properties.getFailedRecordMaxAttempts();
        }
    }

    public long cleanupExpiredFailures(Instant cutoff, int limit) {
        return redisRepository.cleanupExpiredFailures(cutoff.toEpochMilli(), limit);
    }

    private void registerPendingRemovals(List<ClubPopularityRedisRepository.PendingRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        Runnable remove = () -> records.forEach(record ->
                redisRepository.removePendingIfUnchanged(record.member(), record.scoreMillis()));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    remove.run();
                }
            });
        } else {
            remove.run();
        }
    }

    private Map<Long, Club> loadClubs(List<ClubPopularityRedisRepository.PendingRecord> records) {
        Set<Long> ids = new HashSet<>();
        for (ClubPopularityRedisRepository.PendingRecord record : records) {
            try {
                ids.add(ClubPopularityPendingKey.parse(record.member()).clubId());
            } catch (IllegalArgumentException ignored) {
                // 형식 오류는 저장하지 않고 다음 배치에서 별도 지표로 확인한다.
            }
        }
        Map<Long, Club> result = new HashMap<>();
        for (Club club : clubRepository.findAllById(ids)) {
            result.put(club.getId(), club);
        }
        return result;
    }

    public record FlushResult(int saved, int removed, int missingClubs, int isolatedFailures, boolean skipped) {
        public static FlushResult lockSkipped() {
            return new FlushResult(0, 0, 0, 0, true);
        }
    }
}
