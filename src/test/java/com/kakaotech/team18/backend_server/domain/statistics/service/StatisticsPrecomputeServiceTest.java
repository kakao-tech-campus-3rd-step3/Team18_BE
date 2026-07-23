package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("통계 사전 계산")
class StatisticsPrecomputeServiceTest {

    private static final Long FORM_ID = 12L;

    @Mock
    ClubApplyFormRepository clubApplyFormRepository;
    @Mock
    StatisticsServiceImpl statisticsService;
    @Mock
    StatisticsCacheStore cacheStore;
    @Mock
    StatisticsSnapshotReader snapshotReader;

    ClubApplyForm form;
    Club club;

    @BeforeEach
    void setUp() {
        club = mock(Club.class);
        form = mock(ClubApplyForm.class);
        lenient().when(form.getId()).thenReturn(FORM_ID);
        lenient().when(form.getClub()).thenReturn(club);

        // 기본은 모집 진행 중
        lenient().when(club.getRecruitStart()).thenReturn(LocalDateTime.now().minusDays(1));
        lenient().when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().plusDays(1));

        lenient().when(clubApplyFormRepository.findAllForPrecompute(any(), any()))
                .thenReturn(List.of(form));
        lenient().when(cacheStore.tryLock(anyLong(), anyString())).thenReturn(true);
        lenient().when(snapshotReader.exists(anyLong())).thenReturn(false);
    }

    private StatisticsPrecomputeService newService(boolean enabled, int publishStep) {
        StatisticsProperties properties = new StatisticsProperties(
                new StatisticsProperties.Masking(5, 10),
                new StatisticsProperties.Department(5, 30),
                new StatisticsProperties.AdmissionYear(1990),
                new StatisticsProperties.Disclosure(Duration.ofMinutes(5)),
                new StatisticsProperties.Precompute(
                        enabled, publishStep, Duration.ofHours(2), Duration.ofMinutes(5))
        );
        return new StatisticsPrecomputeService(
                clubApplyFormRepository, statisticsService, cacheStore, snapshotReader, properties);
    }

    private void givenComputedTotal(long total) {
        when(statisticsService.calculate(any(), any())).thenReturn(
                new StatisticsResponseDto(FORM_ID, total, false, OffsetDateTime.now(), List.of(), null));
    }

    @Nested
    @DisplayName("공개 갱신 단위")
    class PublishStep {

        @DisplayName("공개 이력이 없으면 첫 집계를 그대로 공개한다")
        @Test
        void publishesFirstTime() {
            givenComputedTotal(20);
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.empty());

            assertThat(newService(true, 1).precomputeAll()).isEqualTo(1);
            verify(cacheStore).put(eq(FORM_ID), any());
        }

        @DisplayName("증가분이 공개 갱신 단위 미만이면 직전 공개본을 유지한다")
        @Test
        void keepsPreviousWhenBelowPublishStep() {
            givenComputedTotal(22);
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.of(20));

            // publishStep = 5, 증가분 2
            assertThat(newService(true, 5).precomputeAll()).isZero();
            verify(cacheStore, never()).put(anyLong(), any());
        }

        @DisplayName("증가분이 공개 갱신 단위에 도달하면 갱신한다")
        @Test
        void publishesWhenReachingPublishStep() {
            givenComputedTotal(25);
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.of(20));

            assertThat(newService(true, 5).precomputeAll()).isEqualTo(1);
            verify(cacheStore).put(eq(FORM_ID), any());
        }

        @DisplayName("신규 지원자가 없으면 캐시를 갱신하지 않는다")
        @Test
        void skipsWhenNoNewApplicants() {
            givenComputedTotal(20);
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.of(20));

            assertThat(newService(true, 1).precomputeAll()).isZero();
            verify(cacheStore, never()).put(anyLong(), any());
        }

        @DisplayName("모집이 종료되면 증가분과 무관하게 최종 수치를 공개한다")
        @Test
        void forcesPublishAfterRecruitmentClosed() {
            when(club.getRecruitStart()).thenReturn(LocalDateTime.now().minusDays(10));
            when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().minusMinutes(1));
            givenComputedTotal(21);
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.of(20));

            // publishStep = 100 이지만 마감됐으므로 공개한다.
            assertThat(newService(true, 100).precomputeAll()).isEqualTo(1);
            verify(cacheStore).put(eq(FORM_ID), any());
        }
    }

    @Nested
    @DisplayName("집계 대상 제외")
    class Skipping {

        @DisplayName("지원자가 없으면 사전 계산 대상에서 제외한다")
        @Test
        void skipsFormWithoutApplicants() {
            givenComputedTotal(0);

            assertThat(newService(true, 1).precomputeAll()).isZero();
            verify(cacheStore, never()).put(anyLong(), any());
        }

        @DisplayName("확정 스냅샷이 있으면 사전 계산하지 않는다")
        @Test
        void skipsWhenSnapshotExists() {
            givenComputedTotal(50);
            when(snapshotReader.exists(FORM_ID)).thenReturn(true);

            assertThat(newService(true, 1).precomputeAll()).isZero();
            verify(cacheStore, never()).put(anyLong(), any());
        }

        @DisplayName("기능 플래그를 끄면 아무 것도 하지 않는다")
        @Test
        void doesNothingWhenDisabled() {
            assertThat(newService(false, 1).precomputeAll()).isZero();

            verify(clubApplyFormRepository, never()).findAllForPrecompute(any(), any());
            verify(statisticsService, never()).calculate(any(), any());
        }
    }

    @Nested
    @DisplayName("다중 인스턴스")
    class MultiInstance {

        @DisplayName("선점 잠금을 얻지 못한 인스턴스는 집계하지 않는다")
        @Test
        void skipsWhenLockNotAcquired() {
            when(cacheStore.tryLock(eq(FORM_ID), anyString())).thenReturn(false);

            assertThat(newService(true, 1).precomputeAll()).isZero();

            verify(statisticsService, never()).calculate(any(), any());
            verify(cacheStore, never()).put(anyLong(), any());
        }

        @DisplayName("같은 주기에 두 인스턴스가 깨어나도 집계는 한 번만 실행된다")
        @Test
        void aggregatesOnlyOnceAcrossInstances() {
            givenComputedTotal(20);
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.empty());
            // 먼저 온 인스턴스만 잠금을 얻는다.
            when(cacheStore.tryLock(eq(FORM_ID), anyString())).thenReturn(true, false);

            StatisticsPrecomputeService instanceA = newService(true, 1);
            StatisticsPrecomputeService instanceB = newService(true, 1);

            int updated = instanceA.precomputeAll() + instanceB.precomputeAll();

            assertThat(updated).isEqualTo(1);
            verify(statisticsService, times(1)).calculate(any(), any());
            verify(cacheStore, times(1)).put(eq(FORM_ID), any());
        }

        @DisplayName("집계가 끝나면 자신이 건 잠금을 해제한다")
        @Test
        void releasesLockAfterAggregation() {
            givenComputedTotal(20);
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.empty());

            newService(true, 1).precomputeAll();

            verify(cacheStore).unlock(eq(FORM_ID), anyString());
        }
    }

    @Nested
    @DisplayName("집계 실행 횟수")
    class AggregationFrequency {

        @DisplayName("지원 건수가 늘어도 집계 횟수는 실행 주기 수에만 비례한다")
        @Test
        void aggregationCountFollowsScheduleNotApplicationCount() {
            when(cacheStore.findPublishedTotal(FORM_ID)).thenReturn(OptionalLong.empty());
            // 주기 사이에 지원자가 1명에서 500명으로 늘어난 상황
            when(statisticsService.calculate(any(), any())).thenReturn(
                    new StatisticsResponseDto(FORM_ID, 1L, false, OffsetDateTime.now(), List.of(), null),
                    new StatisticsResponseDto(FORM_ID, 500L, false, OffsetDateTime.now(), List.of(), null));

            StatisticsPrecomputeService service = newService(true, 1);
            service.precomputeAll();
            service.precomputeAll();

            // 지원 건수가 아니라 스케줄러 실행 횟수(2회)만큼만 집계한다.
            verify(statisticsService, times(2)).calculate(any(), any());
        }
    }
}
