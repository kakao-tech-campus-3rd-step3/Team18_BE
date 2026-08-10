package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    /** 공개 최소 공개 기준: 전체 지원자 수가 이 값 미만이면 비공개. */
    private static final int MIN_TOTAL = 3;

    private final ClubApplyFormRepository clubApplyFormRepository = mock(ClubApplyFormRepository.class);
    private final StatisticsAggregator aggregator = mock(StatisticsAggregator.class);
    private final StatisticsServiceImpl service =
            new StatisticsServiceImpl(clubApplyFormRepository, aggregator, new StatisticsProperties(MIN_TOTAL));

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
        assertThat(res.masked()).isFalse();
        assertThat(res.results()).hasSize(1);

        StatisticsResponseDto.DimensionResult gender = res.results().get(0);
        assertThat(gender.dimension()).isEqualTo(StatisticsDimension.GENDER);
        assertThat(gender.buckets()).extracting(b -> b.ratio())
                .containsExactly(new BigDecimal("0.605"), new BigDecimal("0.395"));
    }

    @Test
    @DisplayName("공개 조회: 전체 지원자가 최소 공개 기준 미만이면 masked=true·빈 results로 비공개한다")
    void publicView_belowMinTotal_masked() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(2L); // < MIN_TOTAL(3)

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.GENDER));

        assertThat(res.masked()).isTrue();
        assertThat(res.results()).isEmpty();
        assertThat(res.totalApplicants()).isEqualTo(2L); // 분포가 아니므로 전체 수는 노출
        // 비공개면 dimension 집계까지 갈 필요가 없다.
        verify(aggregator, never()).aggregate(any(), any());
    }

    @Test
    @DisplayName("공개 조회: 전체 지원자가 기준 이상이면 masked=false로 정상 노출한다(경계값)")
    void publicView_atMinTotal_notMasked() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(3L); // == MIN_TOTAL(3) → 공개
        when(aggregator.aggregate(form, StatisticsDimension.GENDER)).thenReturn(List.of(
                RawBucket.of("MALE", "남성", 2),
                RawBucket.of("FEMALE", "여성", 1)
        ));

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.GENDER));

        assertThat(res.masked()).isFalse();
        assertThat(res.results().get(0).buckets())
                .extracting(StatisticsResponseDto.Bucket::count)
                .containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("관리자 조회도 존재하지 않는 지원폼이면 예외(404 매핑)")
    void admin_notFound_throws() {
        when(clubApplyFormRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatisticsForAdmin(99L, List.of(StatisticsDimension.GENDER)))
                .isInstanceOf(ClubApplyFormNotFoundException.class);
    }

    @Test
    @DisplayName("관리자 조회는 최소 공개 기준 미만이어도 비공개하지 않고 원본을 그대로 반환한다")
    void admin_returnsRawEvenBelowMinTotal() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(2L); // < MIN_TOTAL(3)이지만 관리자는 무관
        when(aggregator.aggregate(form, StatisticsDimension.GENDER)).thenReturn(List.of(
                RawBucket.of("MALE", "남성", 1),
                RawBucket.of("FEMALE", "여성", 1)
        ));

        StatisticsResponseDto res = service.getStatisticsForAdmin(1L, List.of(StatisticsDimension.GENDER));

        assertThat(res.masked()).isFalse();
        assertThat(res.totalApplicants()).isEqualTo(2L);
        assertThat(res.results().get(0).buckets())
                .extracting(StatisticsResponseDto.Bucket::count)
                .containsExactly(1L, 1L);
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
    @DisplayName("공개 조회(기준 이상): count가 0인 버킷은 실제 0으로 노출되어 '비공개(masked)'와 구분된다")
    void publicView_zeroCountBucketIsDistinctFromMasked() {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(1L);
        when(clubApplyFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(aggregator.countApplicants(1L)).thenReturn(10L); // >= MIN_TOTAL → 공개
        when(aggregator.aggregate(form, StatisticsDimension.DAILY_APPLICATIONS)).thenReturn(List.of(
                RawBucket.of("2026-03-02", "3월 2일", 0)
        ));

        StatisticsResponseDto res = service.getStatistics(1L, List.of(StatisticsDimension.DAILY_APPLICATIONS));

        assertThat(res.masked()).isFalse();
        assertThat(res.results().get(0).buckets().get(0).count()).isZero();
    }
}
