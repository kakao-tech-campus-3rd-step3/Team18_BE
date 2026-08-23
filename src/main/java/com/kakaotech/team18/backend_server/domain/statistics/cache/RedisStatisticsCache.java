package com.kakaotech.team18.backend_server.domain.statistics.cache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 공개 통계 캐시. 지원폼별 전체 결과를 JSON 문자열로 저장하고 TTL로 만료시킨다.
 * <p>
 * Redis 장애·직렬화 오류가 통계 조회를 막지 않도록, 모든 예외를 삼켜 조회는 '미스'로 저장은 '무시'로 처리한다.
 * 그러면 호출부는 실시간 계산으로 폴백한다.
 */
@Slf4j
@Component
public class RedisStatisticsCache implements StatisticsCache {

    /** 캐시 키 접두사. 응답 스키마가 바뀌면 v2 등으로 올려 이전 캐시와 충돌하지 않게 한다. */
    private static final String KEY_PREFIX = "statistics:v1:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final StatisticsProperties properties;

    public RedisStatisticsCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                                StatisticsProperties properties) {
        this.redisTemplate = redisTemplate;
        // 캐시 왕복 시 calculatedAt의 offset(+09:00)이 UTC(Z)로 바뀌지 않도록, 파싱 때 컨텍스트 타임존으로 보정하지 않는다.
        // (그래야 캐시 히트/미스 응답의 시각 표기가 KST로 일관된다.)
        this.objectMapper = objectMapper.copy().disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        this.properties = properties;
    }

    @Override
    public Optional<StatisticsResponseDto> find(Long clubApplyFormId) {
        try {
            String json = redisTemplate.opsForValue().get(key(clubApplyFormId));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, StatisticsResponseDto.class));
        } catch (Exception e) {
            // 캐시 장애/역직렬화 실패는 조회를 막지 않는다. 미스로 간주해 실시간 계산으로 폴백한다.
            log.warn("통계 캐시 조회 실패 clubApplyFormId={} : {}", clubApplyFormId, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public void put(Long clubApplyFormId, StatisticsResponseDto fullStatistics) {
        try {
            String json = objectMapper.writeValueAsString(fullStatistics);
            redisTemplate.opsForValue()
                    .set(key(clubApplyFormId), json, Duration.ofSeconds(properties.cacheTtlSeconds()));
        } catch (Exception e) {
            // 저장 실패도 조회를 막지 않는다. 다음 요청이 다시 계산·저장을 시도한다.
            log.warn("통계 캐시 저장 실패 clubApplyFormId={} : {}", clubApplyFormId, e.toString());
        }
    }

    private String key(Long clubApplyFormId) {
        return KEY_PREFIX + clubApplyFormId;
    }
}
