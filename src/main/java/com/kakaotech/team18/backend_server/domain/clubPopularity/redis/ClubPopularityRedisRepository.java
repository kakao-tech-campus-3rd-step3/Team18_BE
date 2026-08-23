package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
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
    private static final RedisScript<Long> REFRESH_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('PEXPIRE', KEYS[1], ARGV[2]) else return 0 end",
            Long.class);
    private static final RedisScript<Long> REPLACE_KNOWN_CLUBS_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
                redis.call('RENAME', KEYS[1], KEYS[2])
            else
                redis.call('DEL', KEYS[2])
            end
            redis.call('SET', KEYS[3], ARGV[1])
            return 1
            """, Long.class);
    private static final Duration REPLACEMENT_KEY_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public String recoveryStatus() {
        return redisTemplate.opsForValue().get(ClubPopularityRedisKeys.RECOVERY_STATUS);
    }

    public boolean knownClubRegistryReady() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ClubPopularityRedisKeys.KNOWN_CLUBS_READY));
    }

    public Set<String> candidateClubIds() {
        Set<String> members = redisTemplate.opsForSet().intersect(
                ClubPopularityRedisKeys.CANDIDATES, ClubPopularityRedisKeys.KNOWN_CLUBS);
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

    public long pendingCount() {
        Long size = redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.PENDING);
        return size == null ? 0L : size;
    }

    public long oldestPendingAgeSeconds(long nowMillis) {
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().rangeWithScores(ClubPopularityRedisKeys.PENDING, 0, 0);
        if (tuples == null || tuples.isEmpty() || tuples.iterator().next().getScore() == null) {
            return 0L;
        }
        long ageMillis = Math.max(0L, nowMillis - tuples.iterator().next().getScore().longValue());
        return ageMillis / 1000L;
    }

    public RecordResult recordView(long clubId, ClubPopularityViewerIdentity identity, long nowMillis,
            int minIntervalSeconds, int activeTtlSeconds, long recentTtlSeconds) {
        String member = identity.redisMember();
        String pendingMember = ClubPopularityPendingKey.serialize(clubId, identity);
        Long result = redisTemplate.execute(RECORD_VIEWS_SCRIPT,
                List.of(
                        ClubPopularityRedisKeys.recentViewers(clubId),
                        ClubPopularityRedisKeys.activeViewers(clubId),
                        ClubPopularityRedisKeys.PENDING,
                        ClubPopularityRedisKeys.CANDIDATES,
                        ClubPopularityRedisKeys.RECOVERY_STATUS,
                        ClubPopularityRedisKeys.rateLimit("views", clubId, member),
                        ClubPopularityRedisKeys.KNOWN_CLUBS),
                Long.toString(nowMillis), member, pendingMember,
                Integer.toString(minIntervalSeconds), Integer.toString(activeTtlSeconds),
                Long.toString(recentTtlSeconds), Long.toString(clubId));
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
                        ClubPopularityRedisKeys.rateLimit("heartbeat", clubId, member),
                        ClubPopularityRedisKeys.KNOWN_CLUBS),
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

    public Map<Long, ViewerCounts> aggregateAll(Set<Long> clubIds, long nowMillis, long recentCutoffMillis,
            long activeCutoffMillis) {
        if (clubIds.isEmpty()) {
            return Map.of();
        }
        List<Long> orderedIds = new ArrayList<>(clubIds);
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] script = AGGREGATE_SCRIPT.getScriptAsString().getBytes(StandardCharsets.UTF_8);
            for (Long clubId : orderedIds) {
                byte[][] keys = {
                        bytes(ClubPopularityRedisKeys.recentViewers(clubId)),
                        bytes(ClubPopularityRedisKeys.activeViewers(clubId)),
                        bytes(ClubPopularityRedisKeys.CANDIDATES),
                        bytes(ClubPopularityRedisKeys.RECOVERY_STATUS)
                };
                byte[][] args = {
                        bytes(Long.toString(nowMillis)),
                        bytes(Long.toString(recentCutoffMillis)),
                        bytes(Long.toString(activeCutoffMillis)),
                        bytes(Long.toString(clubId))
                };
                byte[][] evalArgs = new byte[keys.length + args.length][];
                System.arraycopy(keys, 0, evalArgs, 0, keys.length);
                System.arraycopy(args, 0, evalArgs, keys.length, args.length);
                connection.scriptingCommands().eval(script, ReturnType.INTEGER, keys.length, evalArgs);
            }
            return null;
        });
        Map<Long, ViewerCounts> counts = new LinkedHashMap<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            Object result = results.get(i);
            if (result instanceof Number number) {
                long packed = number.longValue();
                counts.put(orderedIds.get(i), new ViewerCounts((int) (packed >>> 32), (int) packed));
            }
        }
        return counts;
    }

    public boolean removePendingIfUnchanged(String pendingMember, long scoreMillis) {
        Long result = redisTemplate.execute(CONDITIONAL_REMOVE_SCRIPT,
                List.of(ClubPopularityRedisKeys.PENDING), pendingMember, Long.toString(scoreMillis));
        return result != null && result == 1L;
    }

    public void saveFailedRecord(String failureId, PendingRecord record, String reason, long retryAtMillis,
            Duration retention) {
        redisTemplate.opsForHash().putAll(ClubPopularityRedisKeys.failedData(failureId), Map.of(
                "member", record.member(),
                "scoreMillis", Long.toString(record.scoreMillis()),
                "reason", reason,
                "attempt", "0"));
        redisTemplate.expire(ClubPopularityRedisKeys.failedData(failureId), retention);
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.FAILED_RETRY, failureId, retryAtMillis);
    }

    public Set<String> dueFailedRecords(long nowMillis, int limit) {
        Set<String> ids = redisTemplate.opsForZSet().rangeByScore(ClubPopularityRedisKeys.FAILED_RETRY,
                Double.NEGATIVE_INFINITY, nowMillis, 0, limit);
        return ids == null ? Set.of() : ids;
    }

    public long failedRecordCount(long nowMillis) {
        Long count = redisTemplate.opsForZSet().count(ClubPopularityRedisKeys.FAILED_RETRY,
                Double.NEGATIVE_INFINITY, nowMillis);
        return count == null ? 0L : count;
    }

    public Map<Object, Object> failedRecord(String failureId) {
        return redisTemplate.opsForHash().entries(ClubPopularityRedisKeys.failedData(failureId));
    }

    public void rescheduleFailedRecord(String failureId, int attempt, long retryAtMillis, Duration retention) {
        redisTemplate.opsForHash().put(ClubPopularityRedisKeys.failedData(failureId), "attempt", Integer.toString(attempt));
        redisTemplate.expire(ClubPopularityRedisKeys.failedData(failureId), retention);
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.FAILED_RETRY, failureId, retryAtMillis);
    }

    public void removeFailedRecord(String failureId) {
        redisTemplate.delete(ClubPopularityRedisKeys.failedData(failureId));
        redisTemplate.opsForZSet().remove(ClubPopularityRedisKeys.FAILED_RETRY, failureId);
    }

    public long cleanupExpiredFailures(long cutoffMillis, int limit) {
        Set<String> expired = redisTemplate.opsForZSet().rangeByScore(ClubPopularityRedisKeys.FAILED_RETRY,
                Double.NEGATIVE_INFINITY, cutoffMillis, 0, limit);
        if (expired == null || expired.isEmpty()) {
            return 0;
        }
        redisTemplate.delete(expired.stream().map(ClubPopularityRedisKeys::failedData).toList());
        redisTemplate.opsForZSet().remove(ClubPopularityRedisKeys.FAILED_RETRY, expired.toArray());
        return expired.size();
    }

    public boolean tryAcquireRecoveryLock(String owner, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                ClubPopularityRedisKeys.RECOVERY_LOCK, owner, ttl));
    }

    public boolean ownsRecoveryLock(String owner) {
        return owner.equals(redisTemplate.opsForValue().get(ClubPopularityRedisKeys.RECOVERY_LOCK));
    }

    public boolean refreshRecoveryLock(String owner, Duration ttl) {
        Long result = redisTemplate.execute(REFRESH_LOCK_SCRIPT,
                List.of(ClubPopularityRedisKeys.RECOVERY_LOCK), owner, Long.toString(ttl.toMillis()));
        return result != null && result == 1L;
    }

    public void releaseRecoveryLock(String owner) {
        redisTemplate.execute(RELEASE_LOCK_SCRIPT,
                List.of(ClubPopularityRedisKeys.RECOVERY_LOCK), owner);
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

    /** 요청 경로의 DB 조회 없이 사용할 수 있는 서버 기준 동아리 ID 목록이다. */
    public void replaceKnownClubIds(Collection<Long> clubIds) {
        String replacementKey = ClubPopularityRedisKeys.KNOWN_CLUBS + ":replacement:" + UUID.randomUUID();
        try {
            if (!clubIds.isEmpty()) {
                redisTemplate.opsForSet().add(replacementKey,
                        clubIds.stream().map(String::valueOf).toArray(String[]::new));
                // Lua 교체 전에 통신이 끊겨도 임시 키가 영구히 남지 않도록 한다.
                redisTemplate.expire(replacementKey, REPLACEMENT_KEY_TTL);
            }
            redisTemplate.execute(REPLACE_KNOWN_CLUBS_SCRIPT,
                    List.of(replacementKey, ClubPopularityRedisKeys.KNOWN_CLUBS,
                            ClubPopularityRedisKeys.KNOWN_CLUBS_READY), "1");
        } catch (RuntimeException exception) {
            cleanupReplacementKey(replacementKey, exception);
            throw exception;
        }
    }

    private void cleanupReplacementKey(String replacementKey, RuntimeException originalException) {
        try {
            redisTemplate.delete(replacementKey);
        } catch (RuntimeException cleanupException) {
            originalException.addSuppressed(cleanupException);
        }
    }

    public void rebuildRecentViewer(long clubId, String member, long scoreMillis, long recentTtlSeconds) {
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.recentViewers(clubId), member, scoreMillis);
        redisTemplate.expire(ClubPopularityRedisKeys.recentViewers(clubId), Duration.ofSeconds(recentTtlSeconds));
        redisTemplate.opsForSet().add(ClubPopularityRedisKeys.CANDIDATES, Long.toString(clubId));
    }

    private int clearKeysByPattern(String pattern) {
        final int batchSize = 500;
        int[] deleted = {0};
        redisTemplate.execute(connection -> {
            List<byte[]> batch = new ArrayList<>(batchSize);
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(500).build())) {
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() == batchSize) {
                        deleted[0] += Math.toIntExact(connection.keyCommands().del(batch.toArray(byte[][]::new)));
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    deleted[0] += Math.toIntExact(connection.keyCommands().del(batch.toArray(byte[][]::new)));
                }
            }
            return null;
        }, true);
        return deleted[0];
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
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
        RECOVERING,
        INVALID_CLUB;

        static RecordResult from(Long value) {
            if (value == null) {
                throw new IllegalStateException("Redis record script returned no result");
            }
            return switch (value.intValue()) {
                case 1 -> RECORDED;
                case 2 -> RATE_LIMITED;
                case 3 -> RECOVERING;
                case 4 -> INVALID_CLUB;
                default -> throw new IllegalStateException("Unknown Redis record result: " + value);
            };
        }
    }

    public record ViewerCounts(int recentViewerCount, int activeViewerCount) {
    }

    public record PendingRecord(String member, long scoreMillis) {
    }
}
