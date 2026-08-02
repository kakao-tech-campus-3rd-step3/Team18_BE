package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.dto.ClubPopularityResponse;
import com.kakaotech.team18.backend_server.domain.clubPopularity.dto.PopularClubResponse;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityTimePolicy;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
class ClubPopularityQueryServiceTest {

    @Mock ClubPopularityTimePolicy timePolicy;
    @Mock ClubPopularityRedisRepository redisRepository;

    private ClubPopularityProperties properties;
    private ClubPopularityQueryService service;

    @BeforeEach
    void setUp() {
        properties = new ClubPopularityProperties();
        service = new ClubPopularityQueryService(properties, timePolicy, redisRepository);
        lenient().when(timePolicy.currentInstant()).thenReturn(Instant.ofEpochMilli(1_700_000_000_000L));
    }

    @Test
    void returnsBothBadgesAndAllPopularCandidatesWithoutSortingOrLimiting() {
        when(redisRepository.recoveryStatus()).thenReturn("READY");
        when(redisRepository.candidateClubIds()).thenReturn(new LinkedHashSet<>(List.of("20", "7")));
        when(redisRepository.aggregate(org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new ClubPopularityRedisRepository.ViewerCounts(11, 0));
        when(redisRepository.aggregate(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new ClubPopularityRedisRepository.ViewerCounts(0, 3));

        ClubPopularityResponse response = service.getPopularClubs();

        assertThat(response.clubs()).containsExactly(
                new PopularClubResponse(20, 11, 0, true, false),
                new PopularClubResponse(7, 0, 3, false, true));
    }

    @Test
    void returnsEmptyWhenDisabledOrRecoveringOrRedisFails() {
        properties.setEnabled(false);
        assertThat(service.getPopularClubs().clubs()).isEmpty();
        verify(redisRepository, never()).recoveryStatus();

        properties.setEnabled(true);
        when(redisRepository.recoveryStatus()).thenReturn("RECOVERING");
        assertThat(service.getPopularClubs().clubs()).isEmpty();

        when(redisRepository.recoveryStatus()).thenThrow(new DataAccessResourceFailureException("down"));
        assertThat(service.getPopularClubs().clubs()).isEmpty();
    }

    @Test
    void returnsEmptyIfRecoveryStartsDuringAggregation() {
        when(redisRepository.recoveryStatus()).thenReturn("READY", "RECOVERING");
        when(redisRepository.candidateClubIds()).thenReturn(java.util.Set.of("7"));
        when(redisRepository.aggregate(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new ClubPopularityRedisRepository.ViewerCounts(10, 3));

        assertThat(service.getPopularClubs().clubs()).isEmpty();
    }

    @Test
    void returnsEmptyWhenRecoveryStatusIsMissingOrThereAreNoCandidates() {
        when(redisRepository.recoveryStatus()).thenReturn(null);
        assertThat(service.getPopularClubs().clubs()).isEmpty();

        when(redisRepository.recoveryStatus()).thenReturn("READY", "READY");
        when(redisRepository.candidateClubIds()).thenReturn(java.util.Set.of());
        assertThat(service.getPopularClubs().clubs()).isEmpty();
    }
}
