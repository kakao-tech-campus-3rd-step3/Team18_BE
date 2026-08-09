package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StatisticsServiceImpl - 통계 조회")
class StatisticsServiceImplTest {

    private static final int K = 5;

    private final ClubApplyFormRepository clubApplyFormRepository = mock(ClubApplyFormRepository.class);
    private final StatisticsAggregator aggregator = mock(StatisticsAggregator.class);
    private final StatisticsServiceImpl service =
            new StatisticsServiceImpl(clubApplyFormRepository, aggregator, new StatisticsProperties(K));

    @Test
    @DisplayName("존재하지 않는 지원폼이면 예외(404 매핑)")
    void notFound_throws() {
        when(clubApplyFormRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatistics(99L, List.of(StatisticsDimension.GENDER)))
                .isInstanceOf(ClubApplyFormNotFoundException.class);
    }

    @Test
    @DisplayName("집계기가 준 버킷을 응답으로 조립하고 비율을 소수점 3자리로 채운다")
    void assemblesBucketsWithRatio() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(200L);
        when(aggregator.aggregate(form, StatisticsDimension.GENDER)).thenReturn(List.of(
                RawBucket.of("MALE", "남성", 121),
                RawBucket.of("FEMALE", "여성", 79)
        ));

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.GENDER));

        assertThat(res.clubApplyFormId()).isEqualTo(1L);
        assertThat(res.totalApplicants()).isEqualTo(200L);
        assertThat(res.snapshot()).isFalse();
        assertThat(res.results()).hasSize(1);

        StatisticsResponseDto.DimensionResult gender = res.results().get(0);
        assertThat(gender.dimension()).isEqualTo(StatisticsDimension.GENDER);
        assertThat(gender.buckets()).extracting(b -> b.ratio())
                .containsExactly(new BigDecimal("0.605"), new BigDecimal("0.395"));
    }

    @Test
    @DisplayName("지원자가 0명이면 빈 버킷과 totalApplicants=0을 반환한다")
    void zeroApplicants_emptyButDimensionKept() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(0L);
        when(aggregator.aggregate(form, StatisticsDimension.GENDER)).thenReturn(List.of());

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.GENDER));

        assertThat(res.totalApplicants()).isZero();
        assertThat(res.results()).hasSize(1);
        assertThat(res.results().get(0).buckets()).isEmpty();
    }

    @Test
    @DisplayName("관리자 조회도 존재하지 않는 지원폼이면 예외(404 매핑)")
    void admin_notFound_throws() {
        when(clubApplyFormRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatisticsForAdmin(99L, List.of(StatisticsDimension.GENDER)))
                .isInstanceOf(ClubApplyFormNotFoundException.class);
    }

    @Test
    @DisplayName("관리자 조회는 마스킹 없이 집계기 원본 수치를 그대로 반환한다")
    void admin_returnsRawAggregatedValues() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(3L);
        // 소수 버킷(재식별 위험)이 공개 경로에서는 마스킹 대상이 될 수 있으나 관리자 경로는 원본을 노출한다.
        when(aggregator.aggregate(form, StatisticsDimension.GENDER)).thenReturn(List.of(
                RawBucket.of("MALE", "남성", 2),
                RawBucket.of("FEMALE", "여성", 1)
        ));

        StatisticsResponseDto res = service.getStatisticsForAdmin(1L, List.of(StatisticsDimension.GENDER));

        assertThat(res.totalApplicants()).isEqualTo(3L);
        assertThat(res.results().get(0).buckets())
                .extracting(StatisticsResponseDto.Bucket::count)
                .containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("시계열(DAILY_APPLICATIONS) 버킷은 비율(ratio)을 채우지 않는다")
    void timeSeriesDimension_hasNoRatio() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(10L);
        when(aggregator.aggregate(form, StatisticsDimension.DAILY_APPLICATIONS)).thenReturn(List.of(
                RawBucket.of("2026-03-02", "3월 2일", 4)
        ));

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.DAILY_APPLICATIONS));

        StatisticsResponseDto.DimensionResult daily = res.results().get(0);
        assertThat(daily.type()).isEqualTo(DimensionType.TIME_SERIES);
        assertThat(daily.buckets()).hasSize(1);
        assertThat(daily.buckets().get(0).ratio()).isNull();
    }

    @Test
    @DisplayName("공개 조회는 인원이 k 미만인 버킷의 count/ratio를 마스킹(null)하고, k 이상은 그대로 둔다")
    void publicView_masksBucketsBelowThreshold() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(100L);
        when(aggregator.aggregate(form, StatisticsDimension.GENDER)).thenReturn(List.of(
                RawBucket.of("MALE", "남성", 97),   // k 이상 → 노출
                RawBucket.of("FEMALE", "여성", 3)   // 0 < count < k → 마스킹
        ));

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.GENDER));

        List<StatisticsResponseDto.Bucket> buckets = res.results().get(0).buckets();
        // 버킷 자체는 남기되(요청 항목 존재 신호 유지) 소수 버킷의 수치만 가린다.
        assertThat(buckets).hasSize(2);
        StatisticsResponseDto.Bucket male = buckets.get(0);
        StatisticsResponseDto.Bucket female = buckets.get(1);

        assertThat(male.count()).isEqualTo(97L);
        assertThat(male.ratio()).isEqualTo(new BigDecimal("0.970"));

        assertThat(female.key()).isEqualTo("FEMALE");
        assertThat(female.label()).isEqualTo("여성");
        assertThat(female.count()).isNull();
        assertThat(female.ratio()).isNull();
    }

    @Test
    @DisplayName("count가 0인 버킷(시계열 zero-fill 등)은 식별 대상이 없어 마스킹하지 않는다")
    void publicView_doesNotMaskZeroCountBuckets() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(10L);
        when(aggregator.aggregate(form, StatisticsDimension.DAILY_APPLICATIONS)).thenReturn(List.of(
                RawBucket.of("2026-03-02", "3월 2일", 0)
        ));

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.DAILY_APPLICATIONS));

        assertThat(res.results().get(0).buckets().get(0).count()).isZero();
    }

    @Test
    @DisplayName("관리자 조회는 k와 무관하게 소수 버킷도 마스킹하지 않는다")
    void adminView_doesNotMaskSmallBuckets() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(100L);
        when(aggregator.aggregate(form, StatisticsDimension.GENDER)).thenReturn(List.of(
                RawBucket.of("MALE", "남성", 97),
                RawBucket.of("FEMALE", "여성", 3)
        ));

        StatisticsResponseDto res = service.getStatisticsForAdmin(1L, List.of(StatisticsDimension.GENDER));

        assertThat(res.results().get(0).buckets())
                .extracting(StatisticsResponseDto.Bucket::count)
                .containsExactly(97L, 3L);
    }
}
