package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@SuppressWarnings({"unchecked", "rawtypes"})
class ClubPopularityRedisRepositoryTest {

    @Test
    @DisplayName("known clubs 원자 교체가 실패하면 임시 키를 정리한다")
    void cleansReplacementKeyWhenAtomicReplacementFails() {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOperations = Mockito.mock(SetOperations.class);
        ClubPopularityRedisRepository repository = new ClubPopularityRedisRepository(redisTemplate);
        IllegalStateException failure = new IllegalStateException("Redis script failed");

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        doThrow(failure).when(redisTemplate).execute(any(RedisScript.class), anyList(), eq("1"));

        assertThatThrownBy(() -> repository.replaceKnownClubIds(Set.of(7L)))
                .isSameAs(failure);

        ArgumentCaptor<String> replacementKey = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).delete(replacementKey.capture());
        assertThat(replacementKey.getValue())
                .startsWith(ClubPopularityRedisKeys.KNOWN_CLUBS + ":replacement:");
        verify(redisTemplate).expire(eq(replacementKey.getValue()), eq(Duration.ofMinutes(5)));
    }
}
