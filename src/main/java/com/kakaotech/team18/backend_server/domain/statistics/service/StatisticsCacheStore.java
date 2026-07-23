package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import java.util.Optional;
import java.util.OptionalLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 사전 계산된 통계를 Redis에 보관합니다.
 * <p>
 * 모든 사용자가 같은 데이터를 보므로 지원폼당 캐시 엔트리 하나가 전체 조회 트래픽을 흡수한다. 그 덕에 집계
 * 쿼리 수가 조회 수와 무관해지고, 마감 직전 조회 폭주가 지원서 제출 트래픽과 DB를 두고 경합하지 않는다.
 * <p>
 * <strong>Redis 장애가 통계 조회 실패로 번지지 않게 한다.</strong> 모든 접근은 실패해도 예외를 던지지 않고
 * 비어 있는 결과로 취급하며, 호출한 쪽이 직접 집계하는 경로로 넘어간다. 통계는 부가 기능이므로 Redis가
 * 흔들린다고 응답이 깨져서는 안 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsCacheStore {

    /**
     * 키 접두사의 {@code v1}은 집계 규칙 버전이다. 규칙이 바뀌어 기존 캐시를 그대로 쓰면 안 될 때 이 값을
     * 올리면 예전 엔트리를 자연히 무시할 수 있다.
     */
    private static final String KEY_PREFIX = "statistics:v1:form:";

    private static final String PAYLOAD_SUFFIX = ":payload";
    private static final String PUBLISHED_TOTAL_SUFFIX = ":published-total";
    private static final String LOCK_PREFIX = "statistics:v1:lock:form:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final StatisticsProperties properties;

    /**
     * 캐시된 통계 응답을 읽습니다.
     *
     * @return 캐시가 없거나 Redis 접근에 실패하면 {@link Optional#empty()}
     */
    public Optional<StatisticsResponseDto> find(Long clubApplyFormId) {
        try {
            String json = redisTemplate.opsForValue().get(payloadKey(clubApplyFormId));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, StatisticsResponseDto.class));
        } catch (Exception e) {
            log.warn("통계 캐시 조회 실패. 직접 집계로 넘어갑니다. clubApplyFormId={}, error={}",
                    clubApplyFormId, e.toString());
            return Optional.empty();
        }
    }

    /**
     * 집계 결과를 캐시에 기록하고, 이번에 공개한 누적 지원자 수를 함께 남깁니다.
     * <p>
     * 공개 시점의 지원자 수를 저장해 두어야 다음 집계 때 "직전 공개 대비 얼마나 늘었는지"를 판단할 수 있다.
     */
    public void put(Long clubApplyFormId, StatisticsResponseDto payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue()
                    .set(payloadKey(clubApplyFormId), json, properties.precompute().cacheTtl());
            redisTemplate.opsForValue()
                    .set(publishedTotalKey(clubApplyFormId), String.valueOf(payload.totalApplicants()),
                            properties.precompute().cacheTtl());
        } catch (Exception e) {
            log.warn("통계 캐시 기록 실패. clubApplyFormId={}, error={}", clubApplyFormId, e.toString());
        }
    }

    /**
     * 직전에 공개한 누적 지원자 수.
     *
     * @return 공개 이력이 없거나 Redis 접근에 실패하면 비어 있음
     */
    public OptionalLong findPublishedTotal(Long clubApplyFormId) {
        try {
            String value = redisTemplate.opsForValue().get(publishedTotalKey(clubApplyFormId));
            return value == null ? OptionalLong.empty() : OptionalLong.of(Long.parseLong(value));
        } catch (Exception e) {
            log.warn("직전 공개 지원자 수 조회 실패. clubApplyFormId={}, error={}", clubApplyFormId, e.toString());
            return OptionalLong.empty();
        }
    }

    /**
     * 지원폼 단위 선점 잠금을 시도합니다.
     * <p>
     * 여러 인스턴스가 같은 스케줄에 깨어나므로, 잠금이 없으면 동일 지원폼을 인스턴스 수만큼 중복 집계한다.
     * TTL을 두어 집계 중 인스턴스가 죽어도 잠금이 영구히 남지 않게 한다.
     *
     * @return 잠금을 얻었으면 true. Redis 접근에 실패하면 false를 반환해 이번 주기를 건너뛴다.
     */
    public boolean tryLock(Long clubApplyFormId, String token) {
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey(clubApplyFormId), token, properties.precompute().lockTtl());
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("통계 집계 잠금 획득 실패. 이번 주기를 건너뜁니다. clubApplyFormId={}, error={}",
                    clubApplyFormId, e.toString());
            return false;
        }
    }

    /**
     * 자신이 건 잠금만 해제합니다.
     * <p>
     * 토큰을 비교하는 이유는, TTL이 먼저 만료돼 다른 인스턴스가 잠금을 새로 잡은 상황에서 뒤늦게 끝난 쪽이
     * 남의 잠금을 풀어버리는 것을 막기 위해서다.
     */
    public void unlock(Long clubApplyFormId, String token) {
        try {
            String key = lockKey(clubApplyFormId);
            if (token.equals(redisTemplate.opsForValue().get(key))) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.warn("통계 집계 잠금 해제 실패. TTL 만료를 기다립니다. clubApplyFormId={}, error={}",
                    clubApplyFormId, e.toString());
        }
    }

    private String payloadKey(Long clubApplyFormId) {
        return KEY_PREFIX + clubApplyFormId + PAYLOAD_SUFFIX;
    }

    private String publishedTotalKey(Long clubApplyFormId) {
        return KEY_PREFIX + clubApplyFormId + PUBLISHED_TOTAL_SUFFIX;
    }

    private String lockKey(Long clubApplyFormId) {
        return LOCK_PREFIX + clubApplyFormId;
    }
}
