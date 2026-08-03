package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.repository.ClubViewBatchRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ClubPopularityPersistenceServiceTest {

    @Mock ClubPopularityRedisRepository redisRepository;
    @Mock ClubViewBatchRepository clubViewRepository;
    @Mock ClubRepository clubRepository;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock ClubPopularityProperties properties;

    private ClubPopularityPersistenceService service;

    @BeforeEach
    void setUp() {
        service = new ClubPopularityPersistenceService(redisRepository, clubViewRepository, clubRepository, redisTemplate, properties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.setIfAbsent(eq("club:popularity:lock:flush"), any(String.class), any()))
                .thenReturn(true);
    }

    @Test
    void savesOnlyExistingClubsAndConditionallyRemovesPendingRows() {
        Club club = org.mockito.Mockito.mock(Club.class);
        when(club.getId()).thenReturn(7L);
        when(redisRepository.pendingRecords(500)).thenReturn(List.of(
                new ClubPopularityRedisRepository.PendingRecord("v1|7|U|15", 1_700_000_000_000L),
                new ClubPopularityRedisRepository.PendingRecord("v1|99|U|15", 1_700_000_000_000L)));
        when(clubRepository.findAllById(any())).thenReturn(List.of(club));
        when(redisRepository.removePendingIfUnchanged(any(), eq(1_700_000_000_000L))).thenReturn(true);

        ClubPopularityPersistenceService.FlushResult result = service.flushPending(500);

        assertThat(result.saved()).isEqualTo(1);
        assertThat(result.missingClubs()).isEqualTo(1);
        assertThat(result.removed()).isEqualTo(2);
        verify(clubViewRepository).upsertUser(eq(7L), eq(15L), any());
    }

    @Test
    void preservesPendingWhenAnotherWorkerOwnsTheLock() {
        when(valueOperations.setIfAbsent(eq("club:popularity:lock:flush"), any(String.class), any()))
                .thenReturn(false);

        ClubPopularityPersistenceService.FlushResult result = service.flushPending(500);

        assertThat(result.skipped()).isTrue();
        verify(redisRepository, never()).pendingRecords(any(Integer.class));
    }
}
