package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.time.Duration;
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
