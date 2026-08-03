package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubPopularityPersistenceService {

    private final ClubPopularityRedisRepository redisRepository;
    @Qualifier("clubViewRepository")
    private final ClubViewBatchRepository clubViewRepository;
    private final ClubRepository clubRepository;
    private final StringRedisTemplate redisTemplate;
    private final ClubPopularityProperties properties;

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
            redisTemplate.delete(ClubPopularityRedisKeys.FLUSH_LOCK);
        }
    }

    @Transactional
    public FlushResult flushLocked(int batchSize) {
        List<ClubPopularityRedisRepository.PendingRecord> records = redisRepository.pendingRecords(batchSize);
        if (records.isEmpty()) {
            return new FlushResult(0, 0, 0, 0, false);
        }

        Map<Long, Club> clubs = loadClubs(records);
        int saved = 0;
        int removed = 0;
        int missing = 0;
        for (ClubPopularityRedisRepository.PendingRecord record : records) {
            ClubPopularityPendingKey.Parsed parsed;
            try {
                parsed = ClubPopularityPendingKey.parse(record.member());
            } catch (IllegalArgumentException exception) {
                isolateFailure(record, exception.getMessage());
                redisRepository.removePendingIfUnchanged(record.member(), record.scoreMillis());
                continue;
            }
            if (!clubs.containsKey(parsed.clubId())) {
                missing++;
                if (redisRepository.removePendingIfUnchanged(record.member(), record.scoreMillis())) {
                    removed++;
                }
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
                redisRepository.removePendingIfUnchanged(record.member(), record.scoreMillis());
                continue;
            }
            saved++;
            if (redisRepository.removePendingIfUnchanged(record.member(), record.scoreMillis())) {
                removed++;
            }
        }
        return new FlushResult(saved, removed, missing, 0, false);
    }

    private void isolateFailure(ClubPopularityRedisRepository.PendingRecord record, String reason) {
        String failureId = UUID.randomUUID().toString();
        redisRepository.saveFailedRecord(failureId, record, reason == null ? "invalid record" : reason,
                System.currentTimeMillis() + 10 * 60 * 1000L);
        log.warn("Club popularity pending record moved to failed store: {}", record.member());
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
            try {
                ClubPopularityRedisRepository.PendingRecord record = new ClubPopularityRedisRepository.PendingRecord(
                        (String) data.get("member"), Long.parseLong((String) data.get("scoreMillis")));
                ClubPopularityPendingKey.Parsed parsed = ClubPopularityPendingKey.parse(record.member());
                if (clubRepository.findById(parsed.clubId()).isEmpty()) {
                    redisRepository.removeFailedRecord(failureId);
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
                int attempt = Integer.parseInt(String.valueOf(data.getOrDefault("attempt", "1")));
                if (attempt >= properties.getFailedRecordMaxAttempts()) {
                    redisRepository.removeFailedRecord(failureId);
                } else {
                    int delayMinutes = properties.getFailedRecordRetryDelaysMinutes().get(attempt);
                    redisRepository.rescheduleFailedRecord(failureId, attempt + 1,
                            System.currentTimeMillis() + delayMinutes * 60_000L);
                }
            }
            processed++;
        }
        return processed;
    }

    public long cleanupExpiredFailures(Instant cutoff) {
        return redisRepository.cleanupExpiredFailures(cutoff.toEpochMilli());
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
