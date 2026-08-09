package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
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

    private final ClubApplyFormRepository clubApplyFormRepository = mock(ClubApplyFormRepository.class);
    private final StatisticsAggregator aggregator = mock(StatisticsAggregator.class);
    private final StatisticsServiceImpl service = new StatisticsServiceImpl(clubApplyFormRepository, aggregator);

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
}
