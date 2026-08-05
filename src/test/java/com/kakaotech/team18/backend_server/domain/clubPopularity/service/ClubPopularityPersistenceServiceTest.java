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
import com.kakaotech.team18.backend_server.domain.clubPopularity.metrics.ClubPopularityMetrics;
import com.kakaotech.team18.backend_server.domain.clubPopularity.repository.ClubViewBatchRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class ClubPopularityPersistenceServiceTest {

    @Mock ClubPopularityRedisRepository redisRepository;
    @Mock ClubViewBatchRepository clubViewRepository;
    @Mock ClubRepository clubRepository;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock ClubPopularityProperties properties;
    @Mock ClubPopularityMetrics metrics;

    private ClubPopularityPersistenceService service;

    @BeforeEach
    void setUp() {
        service = new ClubPopularityPersistenceService(redisRepository, clubViewRepository, clubRepository, redisTemplate, properties, metrics);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.setIfAbsent(eq("club:popularity:lock:flush"), any(String.class), any()))
                .thenReturn(true);
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
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

    @Test
    void countsMalformedRecordsAsIsolatedAndRemoved() {
        when(redisRepository.pendingRecords(500)).thenReturn(List.of(
                new ClubPopularityRedisRepository.PendingRecord("malformed", 1_700_000_000_000L)));
        when(clubRepository.findAllById(any())).thenReturn(List.of());
        when(properties.getFailedRecordRetryDelaysMinutes()).thenReturn(List.of(10));
        when(properties.getFailedRecordRetentionHours()).thenReturn(24);

        ClubPopularityPersistenceService.FlushResult result = service.flushPending(500);

        assertThat(result.removed()).isEqualTo(1);
        assertThat(result.isolatedFailures()).isEqualTo(1);
        verify(redisRepository).saveFailedRecord(any(), any(), eq("invalid pending key format"), any(Long.class), any());
    }

    @Test
    void countsDataIntegrityRecordsAsIsolatedAndRemoved() {
        Club club = org.mockito.Mockito.mock(Club.class);
        when(club.getId()).thenReturn(7L);
        when(redisRepository.pendingRecords(500)).thenReturn(List.of(
                new ClubPopularityRedisRepository.PendingRecord("v1|7|U|15", 1_700_000_000_000L)));
        when(clubRepository.findAllById(any())).thenReturn(List.of(club));
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("duplicate"))
                .when(clubViewRepository).upsertUser(eq(7L), eq(15L), any());
        when(properties.getFailedRecordRetryDelaysMinutes()).thenReturn(List.of(10));
        when(properties.getFailedRecordRetentionHours()).thenReturn(24);

        ClubPopularityPersistenceService.FlushResult result = service.flushPending(500);

        assertThat(result.removed()).isEqualTo(1);
        assertThat(result.isolatedFailures()).isEqualTo(1);
        verify(redisRepository).saveFailedRecord(any(), any(), eq("duplicate"), any(Long.class), any());
    }

    @Test
    void removesFailedRecordOnlyAfterSuccessfulTransactionCommit() {
        Club club = org.mockito.Mockito.mock(Club.class);
        when(clubRepository.findById(7L)).thenReturn(Optional.of(club));
        when(redisRepository.dueFailedRecords(any(Long.class), eq(1))).thenReturn(Set.of("failure-1"));
        when(redisRepository.failedRecord("failure-1")).thenReturn(Map.of(
                "member", "v1|7|U|15",
                "scoreMillis", "1700000000000",
                "attempt", "1"));

        TransactionSynchronizationManager.initSynchronization();
        service.retryFailedRecords(1);

        verify(redisRepository, never()).removeFailedRecord("failure-1");
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(redisRepository).removeFailedRecord("failure-1");
    }

    @Test
    void schedulesFirstFailedRetryUsingTheFirstConfiguredDelay() {
        Club club = org.mockito.Mockito.mock(Club.class);
        when(clubRepository.findById(7L)).thenReturn(Optional.of(club));
        when(redisRepository.dueFailedRecords(any(Long.class), eq(1))).thenReturn(Set.of("failure-1"));
        when(redisRepository.failedRecord("failure-1")).thenReturn(Map.of(
                "member", "v1|7|U|15",
                "scoreMillis", "1700000000000",
                "attempt", "1"));
        org.mockito.Mockito.doThrow(new RuntimeException("db down"))
                .when(clubViewRepository).upsertUser(eq(7L), eq(15L), any());
        when(properties.getFailedRecordMaxAttempts()).thenReturn(3);
        when(properties.getFailedRecordRetryDelaysMinutes()).thenReturn(List.of(10, 60, 360));
        when(properties.getFailedRecordRetentionHours()).thenReturn(25);

        long before = System.currentTimeMillis();
        service.retryFailedRecords(1);
        long after = System.currentTimeMillis();

        verify(redisRepository).rescheduleFailedRecord(eq("failure-1"), eq(2),
                org.mockito.ArgumentMatchers.longThat(value ->
                        value >= before + 10 * 60_000L && value <= after + 10 * 60_000L),
                any());
    }
}
