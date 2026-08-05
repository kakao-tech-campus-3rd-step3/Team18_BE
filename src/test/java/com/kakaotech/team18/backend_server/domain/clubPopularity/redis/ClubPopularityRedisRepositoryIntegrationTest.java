package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Testcontainers
class ClubPopularityRedisRepositoryIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;
    private static ClubPopularityRedisRepository repository;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        repository = new ClubPopularityRedisRepository(redisTemplate);
    }

    @AfterAll
    static void disconnect() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void resetRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        redisTemplate.opsForValue().set(ClubPopularityRedisKeys.RECOVERY_STATUS, "READY");
        repository.replaceKnownClubIds(Set.of(7L, 8L));
    }

    @Test
    @DisplayName("views는 중복을 제거하고 최근·활성·대기·후보 데이터를 원자적으로 기록한다")
    void recordsViewAtomically() {
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.user(15L);
        long now = 1_700_000_000_000L;

        assertThat(repository.recordView(7, identity, now, 5, 180, 90_000))
                .isEqualTo(ClubPopularityRedisRepository.RecordResult.RECORDED);
        assertThat(repository.recordView(7, identity, now + 1_000, 5, 180, 90_000))
                .isEqualTo(ClubPopularityRedisRepository.RecordResult.RATE_LIMITED);
        assertThat(redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.recentViewers(7))).isEqualTo(1);
        assertThat(redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.activeViewers(7))).isEqualTo(1);
        assertThat(redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.PENDING)).isEqualTo(1);
        assertThat(redisTemplate.opsForSet().isMember(ClubPopularityRedisKeys.CANDIDATES, "7")).isTrue();
    }

    @Test
    @DisplayName("서버가 동기화한 동아리 목록 밖의 ID는 Redis 키를 만들지 않는다")
    void rejectsUnknownClubWithoutCreatingKeys() {
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.user(15L);

        assertThat(repository.recordView(999, identity, 1_700_000_000_000L, 5, 180, 90_000))
                .isEqualTo(ClubPopularityRedisRepository.RecordResult.INVALID_CLUB);
        assertThat(repository.recordHeartbeat(999, identity, 1_700_000_000_000L, 1, 180))
                .isEqualTo(ClubPopularityRedisRepository.RecordResult.INVALID_CLUB);
        assertThat(redisTemplate.hasKey(ClubPopularityRedisKeys.recentViewers(999))).isFalse();
        assertThat(redisTemplate.hasKey(ClubPopularityRedisKeys.activeViewers(999))).isFalse();
        assertThat(redisTemplate.opsForSet().isMember(ClubPopularityRedisKeys.CANDIDATES, "999")).isFalse();
    }

    @Test
    @DisplayName("known clubs 교체는 기존 레지스트리 공백 없이 새 기준을 적용한다")
    void replacesKnownClubRegistryWithoutLeavingReplacementKeys() {
        repository.replaceKnownClubIds(Set.of(8L, 9L));

        assertThat(repository.knownClubRegistryReady()).isTrue();
        assertThat(redisTemplate.opsForSet().members(ClubPopularityRedisKeys.KNOWN_CLUBS))
                .containsExactlyInAnyOrder("8", "9");
        assertThat(redisTemplate.keys(ClubPopularityRedisKeys.KNOWN_CLUBS + ":replacement:*")).isEmpty();
        assertThat(repository.recordView(8, ClubPopularityViewerIdentity.user(15L),
                1_700_000_000_000L, 5, 180, 90_000))
                .isEqualTo(ClubPopularityRedisRepository.RecordResult.RECORDED);
        assertThat(repository.recordView(7, ClubPopularityViewerIdentity.user(15L),
                1_700_000_000_000L, 5, 180, 90_000))
                .isEqualTo(ClubPopularityRedisRepository.RecordResult.INVALID_CLUB);
    }

    @Test
    @DisplayName("동시 동일 요청도 하나만 기록하고 최신 시각을 보존한다")
    void concurrentViewsRemainAtomic() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<ClubPopularityRedisRepository.RecordResult>> tasks = java.util.stream.IntStream.range(0, 8)
                    .mapToObj(index -> (Callable<ClubPopularityRedisRepository.RecordResult>) () ->
                            repository.recordView(7, ClubPopularityViewerIdentity.anonymous("same-browser"),
                                    1_700_000_000_000L + index, 60, 180, 90_000))
                    .collect(Collectors.toList());
            List<Future<ClubPopularityRedisRepository.RecordResult>> results = executor.invokeAll(tasks);

            assertThat(results.stream().map(this::get).filter(result -> result ==
                    ClubPopularityRedisRepository.RecordResult.RECORDED).count()).isEqualTo(1);
            assertThat(redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.recentViewers(7))).isEqualTo(1);
            assertThat(redisTemplate.opsForZSet().score(ClubPopularityRedisKeys.recentViewers(7), "A:c2FtZS1icm93c2Vy"))
                    .isBetween(1_700_000_000_000D, 1_700_000_000_007D);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("heartbeat는 활성 사용자만 갱신하고 집계 Lua는 읽기 전용으로 동작한다")
    void heartbeatAndAggregateUseSeparateScope() {
        long now = 1_700_000_000_000L;
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.user(15L);

        assertThat(repository.recordHeartbeat(7, identity, now, 1, 180))
                .isEqualTo(ClubPopularityRedisRepository.RecordResult.RECORDED);
        assertThat(redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.recentViewers(7))).isZero();
        assertThat(redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.activeViewers(7))).isEqualTo(1);

        long later = now + 181_000;
        ClubPopularityRedisRepository.ViewerCounts counts = repository.aggregate(7, later,
                later - 86_400_000, later - 180_000);
        assertThat(counts.recentViewerCount()).isZero();
        assertThat(counts.activeViewerCount()).isZero();
        assertThat(redisTemplate.opsForSet().isMember(ClubPopularityRedisKeys.CANDIDATES, "7")).isTrue();
    }

    @Test
    @DisplayName("여러 후보 집계는 하나의 파이프라인으로 결과를 반환한다")
    void aggregatesCandidatesInPipeline() {
        long now = 1_700_000_000_000L;
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.recentViewers(7), "U:1", now);
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.activeViewers(8), "U:2", now);
        redisTemplate.opsForSet().add(ClubPopularityRedisKeys.CANDIDATES, "7", "8");

        Map<Long, ClubPopularityRedisRepository.ViewerCounts> counts = repository.aggregateAll(
                Set.of(7L, 8L), now, now - 86_400_000, now - 180_000);

        assertThat(counts.get(7L).recentViewerCount()).isEqualTo(1);
        assertThat(counts.get(8L).activeViewerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("대기 데이터는 읽은 시각이 바뀌면 조건부 삭제하지 않는다")
    void conditionalRemovalPreservesNewerPendingValue() {
        long now = 1_700_000_000_000L;
        ClubPopularityViewerIdentity identity = ClubPopularityViewerIdentity.user(15L);
        String pending = ClubPopularityPendingKey.serialize(7, identity);
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.PENDING, pending, now + 1);

        assertThat(repository.removePendingIfUnchanged(pending, now)).isFalse();
        assertThat(redisTemplate.opsForZSet().score(ClubPopularityRedisKeys.PENDING, pending)).isEqualTo(now + 1D);
    }

    @Test
    @DisplayName("복구 초기화는 recent·active 패턴과 후보만 대상으로 하고 다른 키를 보존한다")
    void recoveryClearsOnlyPopularityViewerKeys() {
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.recentViewers(7), "U:1", 10);
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.activeViewers(7), "U:1", 10);
        redisTemplate.opsForZSet().add(ClubPopularityRedisKeys.PENDING, "v1|7|U|1", 10);
        redisTemplate.opsForSet().add(ClubPopularityRedisKeys.CANDIDATES, "7");

        assertThat(repository.clearRecentViewerKeys()).isEqualTo(1);
        repository.clearCandidates();

        assertThat(redisTemplate.hasKey(ClubPopularityRedisKeys.recentViewers(7))).isFalse();
        assertThat(redisTemplate.hasKey(ClubPopularityRedisKeys.activeViewers(7))).isTrue();
        assertThat(redisTemplate.opsForZSet().zCard(ClubPopularityRedisKeys.PENDING)).isEqualTo(1);
        assertThat(redisTemplate.opsForSet().size(ClubPopularityRedisKeys.CANDIDATES)).isZero();
    }

    @Test
    @DisplayName("복구 잠금은 소유자만 해제할 수 있다")
    void recoveryLockIsOwnerBound() {
        assertThat(repository.tryAcquireRecoveryLock("owner-a", java.time.Duration.ofMinutes(1))).isTrue();
        assertThat(repository.ownsRecoveryLock("owner-b")).isFalse();
        repository.releaseRecoveryLock("owner-b");
        assertThat(repository.ownsRecoveryLock("owner-a")).isTrue();
        repository.releaseRecoveryLock("owner-a");
        assertThat(repository.ownsRecoveryLock("owner-a")).isFalse();
    }

    private ClubPopularityRedisRepository.RecordResult get(Future<ClubPopularityRedisRepository.RecordResult> future) {
        try {
            return future.get();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
