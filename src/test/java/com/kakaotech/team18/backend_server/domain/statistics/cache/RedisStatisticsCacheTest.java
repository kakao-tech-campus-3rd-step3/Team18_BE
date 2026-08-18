package com.kakaotech.team18.backend_server.domain.statistics.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@DisplayName("RedisStatisticsCache")
class RedisStatisticsCacheTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    // 운영 ObjectMapper처럼 시간을 타임스탬프가 아닌 ISO 문자열로 직렬화해 offset을 보존한다.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final StatisticsProperties properties = new StatisticsProperties(3, 1800);
    private final RedisStatisticsCache cache = new RedisStatisticsCache(redisTemplate, objectMapper, properties);

    private StatisticsResponseDto sample() {
        return new StatisticsResponseDto(
                12L, 200L, false, false, OffsetDateTime.parse("2026-03-14T23:59:30+09:00"),
                List.of(new StatisticsResponseDto.DimensionResult(
                        StatisticsDimension.GENDER, DimensionType.CATEGORICAL,
                        List.of(new StatisticsResponseDto.Bucket("MALE", "남성", 121L, new BigDecimal("0.605"))))));
    }

    @Test
    @DisplayName("put으로 저장한 값을 find로 되읽으면 원본과 같다(레코드·OffsetDateTime·BigDecimal 직렬화 왕복)")
    void putThenFind_roundTrips() {
        StatisticsResponseDto dto = sample();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        cache.put(12L, dto);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(valueOps)
                .set(eq("statistics:v1:12"), jsonCaptor.capture(), eq(Duration.ofSeconds(1800)));

        when(valueOps.get("statistics:v1:12")).thenReturn(jsonCaptor.getValue());
        Optional<StatisticsResponseDto> found = cache.find(12L);

        assertThat(found).contains(dto);
    }

    @Test
    @DisplayName("캐시에 값이 없으면 빈 Optional")
    void find_miss_returnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("statistics:v1:12")).thenReturn(null);

        assertThat(cache.find(12L)).isEmpty();
    }

    @Test
    @DisplayName("Redis 장애로 조회가 실패해도 예외 대신 빈 Optional로 폴백한다")
    void find_onError_returnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("statistics:v1:12")).thenThrow(new RuntimeException("redis down"));

        assertThat(cache.find(12L)).isEmpty();
    }

    @Test
    @DisplayName("Redis 장애로 저장이 실패해도 예외를 던지지 않는다")
    void put_onError_doesNotThrow() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doThrow(new RuntimeException("redis down")).when(valueOps).set(any(), any(), any(Duration.class));

        assertThatCode(() -> cache.put(12L, sample())).doesNotThrowAnyException();
    }
}
