package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    /** 모든 일자·시각 집계의 기준 시간대. */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 비율의 소수점 자릿수. 모든 dimension에서 동일하게 고정한다. */
    private static final int RATIO_SCALE = 3;

    private final ClubApplyFormRepository clubApplyFormRepository;
    private final StatisticsAggregator aggregator;
    private final StatisticsMasker masker;

    @Override
    public StatisticsResponseDto getStatistics(Long clubApplyFormId, List<StatisticsDimension> dimensions) {
        ClubApplyForm form = clubApplyFormRepository.findById(clubApplyFormId)
                .orElseThrow(() -> new ClubApplyFormNotFoundException("clubApplyFormId = " + clubApplyFormId));

        return calculate(form, dimensions);
    }

    /**
     * 지원폼의 통계를 실제로 집계합니다.
     * <p>
     * 조회 경로와 스케줄러 사전 계산 경로가 같은 결과를 내도록 이 메서드를 공유한다.
     */
    public StatisticsResponseDto calculate(ClubApplyForm form, List<StatisticsDimension> dimensions) {
        long totalApplicants = aggregator.countApplicants(form.getId());

        List<StatisticsResponseDto.DimensionResult> results = new ArrayList<>();

        // 누적 지원자가 최소 공개 기준에 미달하면 속성 분포를 아예 내보내지 않는다.
        // totalApplicants는 그대로 노출하므로, 클라이언트는 '데이터 없음'과 '기준 미달'을 구분할 수 있다.
        if (masker.isPubliclyDisclosable(totalApplicants)) {
            for (StatisticsDimension dimension : dimensions) {
                results.add(buildDimension(form, dimension, totalApplicants));
            }
        } else {
            log.debug("최소 공개 기준 미달로 분포 비공개. clubApplyFormId={}, totalApplicants={}",
                    form.getId(), totalApplicants);
        }

        return new StatisticsResponseDto(
                form.getId(),
                totalApplicants,
                false,
                OffsetDateTime.now(KST),
                results
        );
    }

    /**
     * 단일 dimension을 집계하고 재식별 방지 규칙을 적용합니다.
     */
    private StatisticsResponseDto.DimensionResult buildDimension(
            ClubApplyForm form,
            StatisticsDimension dimension,
            long totalApplicants
    ) {
        List<RawBucket> raw = aggregator.aggregate(form, dimension);
        List<RawBucket> masked = masker.maskSmallBuckets(raw, dimension.getType());

        // 버킷이 하나만 남았다면 '전원이 같은 값'이라는 뜻이므로 dimension 자체를 비공개 처리한다.
        if (masker.shouldWithholdDimension(masked)) {
            return new StatisticsResponseDto.DimensionResult(
                    dimension, dimension.getType(), null, StatisticsMasker.WITHHELD_NOTICE, List.of());
        }

        return toResult(dimension, masked, totalApplicants, null, null);
    }

    /**
     * 원본 버킷을 응답 버킷으로 변환하고 비율을 채웁니다.
     * <p>
     * 시계열은 전체 대비 비율이 의미가 없으므로 ratio를 채우지 않는다.
     */
    private StatisticsResponseDto.DimensionResult toResult(
            StatisticsDimension dimension,
            List<RawBucket> raw,
            long totalApplicants,
            Boolean truncated,
            String notice
    ) {
        boolean withRatio = dimension.getType() != DimensionType.TIME_SERIES;

        List<StatisticsResponseDto.Bucket> buckets = raw.stream()
                .map(b -> new StatisticsResponseDto.Bucket(
                        b.key(),
                        b.label(),
                        b.count(),
                        withRatio ? ratio(b.count(), totalApplicants) : null,
                        b.distinctValues()
                ))
                .toList();

        return new StatisticsResponseDto.DimensionResult(
                dimension,
                dimension.getType(),
                truncated,
                notice,
                buckets
        );
    }

    /**
     * 비율을 소수점 {@value #RATIO_SCALE}자리로 고정해 계산합니다.
     * <p>
     * BigDecimal을 쓰는 이유는 반올림 결과를 고정하고 {@code 0.010}처럼 끝자리 0까지 그대로 직렬화하기 위함이다.
     */
    private BigDecimal ratio(long count, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(RATIO_SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(count)
                .divide(BigDecimal.valueOf(total), RATIO_SCALE, RoundingMode.HALF_UP);
    }
}
