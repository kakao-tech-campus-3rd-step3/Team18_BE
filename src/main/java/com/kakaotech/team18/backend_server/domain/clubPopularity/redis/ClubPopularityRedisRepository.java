package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.time.Duration;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubPopularityRedisRepository {

    private static final RedisScript<Long> RECORD_VIEWS_SCRIPT = script("lua/club-popularity-record-views.lua");
    private static final RedisScript<Long> RECORD_HEARTBEAT_SCRIPT = script("lua/club-popularity-record-heartbeat.lua");
    private static final RedisScript<Long> AGGREGATE_SCRIPT = script("lua/club-popularity-aggregate.lua");
    private static final RedisScript<Long> CONDITIONAL_REMOVE_SCRIPT = script("lua/club-popularity-remove-pending.lua");
    private static final RedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end", Long.class);

    private final StringRedisTemplate redisTemplate;

    public String recoveryStatus() {
        return redisTemplate.opsForValue().get(ClubPopularityRedisKeys.RECOVERY_STATUS);
    }

    public Set<String> candidateClubIds() {
        Set<String> members = redisTemplate.opsForSet().members(ClubPopularityRedisKeys.CANDIDATES);
        return members == null ? Set.of() : members;
    }

    public List<PendingRecord> pendingRecords(int limit) {
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().rangeWithScores(ClubPopularityRedisKeys.PENDING, 0, limit - 1);
        List<PendingRecord> records = new ArrayList<>();
        if (tuples != null) {
            for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple.getValue() != null && tuple.getScore() != null) {
                    records.add(new PendingRecord(tuple.getValue(), tuple.getScore().longValue()));
                }
            }
        }
        return records;
    }

    public RecordResult recordView(long clubId, ClubPopularityViewerIdentity identity, long nowMillis,
            int minIntervalSeconds, int activeTtlSeconds, int recentTtlSeconds) {
        String member = identity.redisMember();
        String pendingMember = ClubPopularityPendingKey.serialize(clubId, identity);
        Long result = redisTemplate.execute(RECORD_VIEWS_SCRIPT,
                List.of(
                        ClubPopularityRedisKeys.recentViewers(clubId),
                        ClubPopularityRedisKeys.activeViewers(clubId),
                        ClubPopularityRedisKeys.PENDING,
                        ClubPopularityRedisKeys.CANDIDATES,
                        ClubPopularityRedisKeys.RECOVERY_STATUS,
                        ClubPopularityRedisKeys.rateLimit("views", clubId, member)),
                Long.toString(nowMillis), member, pendingMember,
                Integer.toString(minIntervalSeconds), Integer.toString(activeTtlSeconds),
                Integer.toString(recentTtlSeconds));
        return RecordResult.from(result);
    }

    public RecordResult recordHeartbeat(long clubId, ClubPopularityViewerIdentity identity, long nowMillis,
            int minIntervalSeconds, int activeTtlSeconds) {
        String member = identity.redisMember();
        Long result = redisTemplate.execute(RECORD_HEARTBEAT_SCRIPT,
                List.of(
                        ClubPopularityRedisKeys.activeViewers(clubId),
                        ClubPopularityRedisKeys.CANDIDATES,
                        ClubPopularityRedisKeys.RECOVERY_STATUS,
                        ClubPopularityRedisKeys.rateLimit("heartbeat", clubId, member)),
                Long.toString(nowMillis), member, Integer.toString(minIntervalSeconds),
                Integer.toString(activeTtlSeconds), Long.toString(clubId));
        return RecordResult.from(result);
    }

    public ViewerCounts aggregate(long clubId, long nowMillis, long recentCutoffMillis, long activeCutoffMillis) {
        Long result = redisTemplate.execute(AGGREGATE_SCRIPT,
                List.of(
                        ClubPopularityRedisKeys.recentViewers(clubId),
                        ClubPopularityRedisKeys.activeViewers(clubId),
                        ClubPopularityRedisKeys.CANDIDATES,
                        ClubPopularityRedisKeys.RECOVERY_STATUS),
                Long.toString(nowMillis), Long.toString(recentCutoffMillis), Long.toString(activeCutoffMillis),
                Long.toString(clubId));
        if (result == null) {
            throw new IllegalStateException("Redis aggregate script returned no result");
        }
        long packed = result;
        return new ViewerCounts((int) (packed >>> 32), (int) packed);
    }

    public boolean removePendingIfUnchanged(String pendingMember, long scoreMillis) {
        Long result = redisTemplate.execute(CONDITIONAL_REMOVE_SCRIPT,
                List.of(ClubPopularityRedisKeys.PENDING), pendingMember, Long.toString(scoreMillis));
        return result != null && result == 1L;
    }

    public void saveFailedRecord(String failureId, PendingRecord record, String reason, long retryAtMillis) {
        redisTemplate.opsForHash().put(ClubPopularityRedisKeys.failedData(failureId), "member", record.member());
        redisTemplate.opsForHash().put(ClubPopularityRedisKeys.failedData(failureId), "scoreMillis",
                Long.toString(record.scoreMillis()));
        redisTemplate.opsForHash().put(ClubPopularityRedisKeys.failedData(failureId), "reason", reason);
        redisTemplate.opsForHash().put(ClubPopularityRedisKeys.failedData(failureId), "attempt", "1");
        redisTemplate.expire(ClubPopularityRedisKeys.failedData(failureId), Duration.ofHours(25));
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.FAILED_RETRY, failureId, retryAtMillis);
    }

    public Set<String> dueFailedRecords(long nowMillis, int limit) {
        Set<String> ids = redisTemplate.opsForZSet().rangeByScore(ClubPopularityRedisKeys.FAILED_RETRY,
                Double.NEGATIVE_INFINITY, nowMillis, 0, limit);
        return ids == null ? Set.of() : ids;
    }

    public Map<Object, Object> failedRecord(String failureId) {
        return redisTemplate.opsForHash().entries(ClubPopularityRedisKeys.failedData(failureId));
    }

    public void rescheduleFailedRecord(String failureId, int attempt, long retryAtMillis) {
        redisTemplate.opsForHash().put(ClubPopularityRedisKeys.failedData(failureId), "attempt", Integer.toString(attempt));
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.FAILED_RETRY, failureId, retryAtMillis);
    }

    public void removeFailedRecord(String failureId) {
        redisTemplate.delete(ClubPopularityRedisKeys.failedData(failureId));
        redisTemplate.opsForZSet().remove(ClubPopularityRedisKeys.FAILED_RETRY, failureId);
    }

    public long cleanupExpiredFailures(long cutoffMillis) {
        Set<String> expired = redisTemplate.opsForZSet().rangeByScore(ClubPopularityRedisKeys.FAILED_RETRY,
                Double.NEGATIVE_INFINITY, cutoffMillis);
        if (expired == null || expired.isEmpty()) {
            return 0;
        }
        for (String failureId : expired) {
            removeFailedRecord(failureId);
        }
        return expired.size();
    }

    public boolean tryAcquireRecoveryLock(String owner, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                "club:popularity:lock:recovery", owner, ttl));
    }

    public boolean ownsRecoveryLock(String owner) {
        return owner.equals(redisTemplate.opsForValue().get("club:popularity:lock:recovery"));
    }

    public boolean refreshRecoveryLock(String owner, Duration ttl) {
        if (!ownsRecoveryLock(owner)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.expire("club:popularity:lock:recovery", ttl));
    }

    public void releaseRecoveryLock(String owner) {
        redisTemplate.execute(RELEASE_LOCK_SCRIPT,
                List.of("club:popularity:lock:recovery"), owner);
    }

    public void setRecoveryStatus(String status) {
        redisTemplate.opsForValue().set(ClubPopularityRedisKeys.RECOVERY_STATUS, status);
    }

    public int clearRecentViewerKeys() {
        return clearKeysByPattern("club:popularity:recent:*");
    }

    public int clearActiveViewerKeys() {
        return clearKeysByPattern("club:popularity:active:*");
    }

    public void clearCandidates() {
        redisTemplate.delete(ClubPopularityRedisKeys.CANDIDATES);
    }

    public void rebuildRecentViewer(long clubId, String member, long scoreMillis) {
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.recentViewers(clubId), member, scoreMillis);
        redisTemplate.opsForSet().add(ClubPopularityRedisKeys.CANDIDATES, Long.toString(clubId));
    }

    private int clearKeysByPattern(String pattern) {
        List<String> keys = new ArrayList<>();
        redisTemplate.execute(connection -> {
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(500).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return null;
        }, true);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return keys.size();
    }

    private static RedisScript<Long> script(String path) {
        try {
            String source = StreamUtils.copyToString(
                    new ClassPathResource(path).getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
            return new DefaultRedisScript<>(source, Long.class);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Unable to load Redis script: " + path, exception);
        }
    }

    public enum RecordResult {
        RECORDED,
        RATE_LIMITED,
        RECOVERING;

        static RecordResult from(Long value) {
            if (value == null) {
                throw new IllegalStateException("Redis record script returned no result");
            }
            return switch (value.intValue()) {
                case 1 -> RECORDED;
                case 2 -> RATE_LIMITED;
                case 3 -> RECOVERING;
                default -> throw new IllegalStateException("Unknown Redis record result: " + value);
            };
        }
    }

    public record ViewerCounts(int recentViewerCount, int activeViewerCount) {
    }

    public record PendingRecord(String member, long scoreMillis) {
    }
}
