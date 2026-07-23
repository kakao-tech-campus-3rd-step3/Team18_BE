package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.DimensionAggregation;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("지원자 통계 조회")
class StatisticsServiceImplTest {

    private static final Long FORM_ID = 12L;

    @Mock
    ClubApplyFormRepository clubApplyFormRepository;
    @Mock
    StatisticsAggregator aggregator;
    @Mock
    StatisticsCacheStore cacheStore;
    @Mock
    StatisticsSnapshotReader snapshotReader;

    ClubApplyForm form;
    StatisticsServiceImpl service;

    @BeforeEach
    void setUp() {
        form = mock(ClubApplyForm.class);
        lenient().when(form.getId()).thenReturn(FORM_ID);
        lenient().when(form.getClub()).thenReturn(mock(Club.class));
        lenient().when(clubApplyFormRepository.findById(FORM_ID)).thenReturn(Optional.of(form));

        service = newService(true);
    }

    /**
     * @param precomputeEnabled 사전 계산 사용 여부
     */
    private StatisticsServiceImpl newService(boolean precomputeEnabled) {
        StatisticsProperties properties = new StatisticsProperties(
                new StatisticsProperties.Masking(5, 10),
                new StatisticsProperties.Department(5, 30),
                new StatisticsProperties.AdmissionYear(1990),
                new StatisticsProperties.Disclosure(Duration.ofMinutes(5)),
                new StatisticsProperties.Precompute(
                        precomputeEnabled, 1, Duration.ofHours(2), Duration.ofMinutes(5))
        );
        return new StatisticsServiceImpl(
                clubApplyFormRepository, aggregator, new StatisticsMasker(properties),
                cacheStore, snapshotReader, properties);
    }

    private void givenNoSnapshotAndNoCache() {
        lenient().when(snapshotReader.find(anyLong())).thenReturn(Optional.empty());
        lenient().when(cacheStore.find(anyLong())).thenReturn(Optional.empty());
    }

    private void givenGenderBuckets(long total, List<RawBucket> buckets) {
        when(aggregator.countApplicants(FORM_ID)).thenReturn(total);
        lenient().when(aggregator.aggregate(any(), any())).thenReturn(DimensionAggregation.empty());
        lenient().when(aggregator.aggregate(form, StatisticsDimension.GENDER))
                .thenReturn(DimensionAggregation.of(buckets));
    }

    private StatisticsResponseDto.DimensionResult resultOf(
            StatisticsResponseDto response, StatisticsDimension dimension) {
        return response.results().stream()
                .filter(r -> r.dimension() == dimension)
                .findFirst()
                .orElseThrow();
    }

    @Nested
    @DisplayName("기본 동작")
    class Basics {

        @DisplayName("존재하지 않는 지원폼은 404로 처리한다")
        @Test
        void throwsWhenFormNotFound() {
            when(clubApplyFormRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getStatistics(999L, StatisticsDimension.defaults()))
                    .isInstanceOf(ClubApplyFormNotFoundException.class);
        }

        @DisplayName("비율은 소수점 3자리로 고정되고 합이 1에 수렴한다")
        @Test
        void ratiosAreFixedToThreeDecimals() {
            givenNoSnapshotAndNoCache();
            givenGenderBuckets(214, List.of(
                    RawBucket.of("MALE", "남성", 121),
                    RawBucket.of("FEMALE", "여성", 91),
                    RawBucket.of("UNKNOWN", "미입력", 2)
            ));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));
            List<StatisticsResponseDto.Bucket> buckets =
                    resultOf(response, StatisticsDimension.GENDER).buckets();

            assertThat(buckets).extracting(StatisticsResponseDto.Bucket::ratio)
                    .containsExactly(
                            new BigDecimal("0.565"), new BigDecimal("0.425"), new BigDecimal("0.009"));
            assertThat(buckets).allSatisfy(b -> assertThat(b.ratio().scale()).isEqualTo(3));

            BigDecimal sum = buckets.stream()
                    .map(StatisticsResponseDto.Bucket::ratio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(sum).isCloseTo(BigDecimal.ONE, org.assertj.core.data.Offset.offset(new BigDecimal("0.002")));
        }

        @DisplayName("버킷 count의 합은 총 지원자 수와 일치한다")
        @Test
        void bucketCountsSumToTotal() {
            givenNoSnapshotAndNoCache();
            givenGenderBuckets(214, List.of(
                    RawBucket.of("MALE", "남성", 121),
                    RawBucket.of("FEMALE", "여성", 91),
                    RawBucket.of("UNKNOWN", "미입력", 2)
            ));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            long sum = resultOf(response, StatisticsDimension.GENDER).buckets().stream()
                    .mapToLong(StatisticsResponseDto.Bucket::count).sum();
            assertThat(sum).isEqualTo(response.totalApplicants());
        }

        @DisplayName("요청한 dimension만 응답에 담는다")
        @Test
        void returnsOnlyRequestedDimensions() {
            givenNoSnapshotAndNoCache();
            givenGenderBuckets(20, List.of(
                    RawBucket.of("MALE", "남성", 12), RawBucket.of("FEMALE", "여성", 8)));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            assertThat(response.results()).extracting(StatisticsResponseDto.DimensionResult::dimension)
                    .containsExactly(StatisticsDimension.GENDER);
        }
    }

    @Nested
    @DisplayName("지원자가 거의 없을 때")
    class FewApplicants {

        @DisplayName("지원자가 0명이면 빈 버킷 배열과 totalApplicants 0을 반환한다")
        @Test
        void zeroApplicants() {
            givenNoSnapshotAndNoCache();
            when(aggregator.countApplicants(FORM_ID)).thenReturn(0L);

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            assertThat(response.totalApplicants()).isZero();
            assertThat(response.results()).isEmpty();
        }

        @DisplayName("지원자가 1명이면 속성 분포를 공개하지 않는다")
        @Test
        void singleApplicantIsNotDisclosed() {
            givenNoSnapshotAndNoCache();
            when(aggregator.countApplicants(FORM_ID)).thenReturn(1L);

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            // 총원은 알려주되 분포는 감춘다. 1명이면 어떤 마스킹을 해도 그 사람의 속성이 그대로 드러난다.
            assertThat(response.totalApplicants()).isEqualTo(1);
            assertThat(response.results()).isEmpty();
        }

        @DisplayName("최소 공개 기준 미만이면 집계 자체를 시도하지 않는다")
        @Test
        void doesNotAggregateBelowMinPublicTotal() {
            givenNoSnapshotAndNoCache();
            when(aggregator.countApplicants(FORM_ID)).thenReturn(9L);

            service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            verify(aggregator, never()).aggregate(any(), any());
        }
    }

    @Nested
    @DisplayName("마스킹 연동")
    class Masking {

        @DisplayName("임계값 미만 버킷은 응답에서 '기타'로 합쳐진다")
        @Test
        void masksSmallBuckets() {
            givenNoSnapshotAndNoCache();
            givenGenderBuckets(50, List.of(
                    RawBucket.of("MALE", "남성", 30),
                    RawBucket.of("FEMALE", "여성", 18),
                    RawBucket.of("UNKNOWN", "미입력", 2)
            ));

            List<StatisticsResponseDto.Bucket> buckets =
                    resultOf(service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER)),
                            StatisticsDimension.GENDER).buckets();

            assertThat(buckets).extracting(StatisticsResponseDto.Bucket::key)
                    .containsExactly("MALE", "FEMALE", StatisticsMasker.OTHERS_KEY);
        }

        @DisplayName("버킷이 하나만 남으면 dimension을 비공개 처리하고 사유를 알린다")
        @Test
        void withholdsSingleBucketDimension() {
            givenNoSnapshotAndNoCache();
            givenGenderBuckets(30, List.of(RawBucket.of("MALE", "남성", 30)));

            StatisticsResponseDto.DimensionResult result =
                    resultOf(service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER)),
                            StatisticsDimension.GENDER);

            assertThat(result.buckets()).isEmpty();
            assertThat(result.notice()).isEqualTo(StatisticsMasker.WITHHELD_NOTICE);
        }
    }

    @Nested
    @DisplayName("캐시와 스냅샷")
    class CacheAndSnapshot {

        @DisplayName("캐시가 있으면 집계 쿼리를 실행하지 않는다")
        @Test
        void readsFromCacheWithoutAggregating() {
            when(snapshotReader.find(FORM_ID)).thenReturn(Optional.empty());
            when(cacheStore.find(FORM_ID)).thenReturn(Optional.of(cached(false, 214)));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            assertThat(response.totalApplicants()).isEqualTo(214);
            verify(aggregator, never()).countApplicants(anyLong());
            verify(aggregator, never()).aggregate(any(), any());
        }

        @DisplayName("캐시 미스면 집계하고 그 결과를 캐시에 채운다")
        @Test
        void computesAndFillsCacheOnMiss() {
            givenNoSnapshotAndNoCache();
            givenGenderBuckets(20, List.of(
                    RawBucket.of("MALE", "남성", 12), RawBucket.of("FEMALE", "여성", 8)));

            service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            verify(cacheStore).put(org.mockito.ArgumentMatchers.eq(FORM_ID), any());
        }

        @DisplayName("스냅샷이 있으면 캐시보다 우선하고 snapshot=true로 표시한다")
        @Test
        void snapshotTakesPrecedenceOverCache() {
            when(snapshotReader.find(FORM_ID)).thenReturn(Optional.of(cached(true, 300)));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            assertThat(response.snapshot()).isTrue();
            assertThat(response.totalApplicants()).isEqualTo(300);
            // 불합격 지원서가 이미 삭제됐을 수 있어 다시 집계하면 왜곡된 값이 나온다.
            verify(aggregator, never()).aggregate(any(), any());
            verify(cacheStore, never()).find(anyLong());
        }

        @DisplayName("사전 계산을 끄면 캐시를 보지 않고 매번 집계한다")
        @Test
        void bypassesCacheWhenPrecomputeDisabled() {
            service = newService(false);
            when(snapshotReader.find(FORM_ID)).thenReturn(Optional.empty());
            givenGenderBuckets(20, List.of(
                    RawBucket.of("MALE", "남성", 12), RawBucket.of("FEMALE", "여성", 8)));

            service.getStatistics(FORM_ID, List.of(StatisticsDimension.GENDER));

            verify(cacheStore, never()).find(anyLong());
            verify(aggregator).countApplicants(FORM_ID);
        }

        private StatisticsResponseDto cached(boolean snapshot, long total) {
            return new StatisticsResponseDto(FORM_ID, total, snapshot, OffsetDateTime.now(), List.of(
                    new StatisticsResponseDto.DimensionResult(
                            StatisticsDimension.GENDER, StatisticsDimension.GENDER.getType(), null, null,
                            List.of(new StatisticsResponseDto.Bucket(
                                    "MALE", "남성", total, new BigDecimal("1.000"), null)))
            ), null);
        }
    }

    @Nested
    @DisplayName("마감 직전 비공개")
    class DeadlineBlackout {

        private Club club;

        @BeforeEach
        void setUpClub() {
            club = mock(Club.class);
            // 스냅샷이 있으면 마감 시각을 보기도 전에 반환하므로, 그 테스트에서는 이 스텁이 쓰이지 않는다.
            lenient().when(form.getClub()).thenReturn(club);
            lenient().when(snapshotReader.find(anyLong())).thenReturn(Optional.empty());
        }

        @DisplayName("마감 5분 전부터는 지원자 수와 분포를 모두 감춘다")
        @Test
        void hidesEverythingInsideBlackoutWindow() {
            when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().plusMinutes(3));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, StatisticsDimension.defaults());

            assertThat(response.totalApplicants()).isNull();
            assertThat(response.results()).isEmpty();
            assertThat(response.notice()).isEqualTo(StatisticsServiceImpl.BLACKOUT_NOTICE);
        }

        @DisplayName("비공개 구간에서는 집계도 캐시 조회도 하지 않는다")
        @Test
        void touchesNothingInsideBlackoutWindow() {
            when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().plusMinutes(1));

            service.getStatistics(FORM_ID, StatisticsDimension.defaults());

            verify(aggregator, never()).countApplicants(anyLong());
            verify(aggregator, never()).aggregate(any(), any());
            verify(cacheStore, never()).find(anyLong());
        }

        @DisplayName("마감 5분보다 이전이면 평소대로 공개한다")
        @Test
        void disclosesBeforeBlackoutWindow() {
            when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().plusMinutes(6));
            when(cacheStore.find(FORM_ID)).thenReturn(Optional.of(
                    new StatisticsResponseDto(FORM_ID, 214L, false, OffsetDateTime.now(), List.of(), null)));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, StatisticsDimension.defaults());

            assertThat(response.totalApplicants()).isEqualTo(214);
            assertThat(response.notice()).isNull();
        }

        @DisplayName("마감 이후에는 다시 공개한다")
        @Test
        void disclosesAgainAfterDeadline() {
            when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().minusMinutes(1));
            when(cacheStore.find(FORM_ID)).thenReturn(Optional.of(
                    new StatisticsResponseDto(FORM_ID, 214L, false, OffsetDateTime.now(), List.of(), null)));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, StatisticsDimension.defaults());

            // 비공개는 마감 직전 구간에 한정된다. 종료된 모집의 최종 수치는 공개한다.
            assertThat(response.totalApplicants()).isEqualTo(214);
        }

        @DisplayName("모집 마감일이 없으면 비공개 구간을 적용하지 않는다")
        @Test
        void noBlackoutWhenDeadlineMissing() {
            when(club.getRecruitEnd()).thenReturn(null);
            when(cacheStore.find(FORM_ID)).thenReturn(Optional.of(
                    new StatisticsResponseDto(FORM_ID, 214L, false, OffsetDateTime.now(), List.of(), null)));

            assertThat(service.getStatistics(FORM_ID, StatisticsDimension.defaults())
                    .totalApplicants()).isEqualTo(214);
        }

        @DisplayName("확정 스냅샷이 있으면 비공개 구간과 무관하게 스냅샷을 반환한다")
        @Test
        void snapshotWinsOverBlackout() {
            when(snapshotReader.find(FORM_ID)).thenReturn(Optional.of(
                    new StatisticsResponseDto(FORM_ID, 300L, true, OffsetDateTime.now(), List.of(), null)));

            StatisticsResponseDto response =
                    service.getStatistics(FORM_ID, StatisticsDimension.defaults());

            assertThat(response.snapshot()).isTrue();
            assertThat(response.totalApplicants()).isEqualTo(300);
        }
    }
}
