package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
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
    private final StatisticsProperties properties;

    @Override
    public StatisticsResponseDto getStatistics(Long clubApplyFormId, List<StatisticsDimension> dimensions) {
        ClubApplyForm form = clubApplyFormRepository.findById(clubApplyFormId)
                .orElseThrow(() -> new ClubApplyFormNotFoundException("clubApplyFormId = " + clubApplyFormId));

        // 공개 통계는 재식별 방지를 위해 최소 공개 기준(k) 미만 버킷을 마스킹한다.
        return maskSmallBuckets(calculate(form, dimensions), properties.minBucketSize());
    }

    @Override
    public StatisticsResponseDto getStatisticsForAdmin(Long clubApplyFormId, List<StatisticsDimension> dimensions) {
        ClubApplyForm form = clubApplyFormRepository.findById(clubApplyFormId)
                .orElseThrow(() -> new ClubApplyFormNotFoundException("clubApplyFormId = " + clubApplyFormId));

        // 관리자용은 마스킹 없이 원본을 그대로 반환한다. 공개 경로(getStatistics)에 이후 마스킹이 붙어도
        // 이 경로는 calculate() 원본만 사용하므로 영향받지 않는다.
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
        for (StatisticsDimension dimension : dimensions) {
            // 지원자가 없으면 버킷은 빈 배열이 된다. dimension 자체는 응답에 그대로 남겨,
            // 클라이언트가 '아직 데이터가 없음'과 '해당 항목을 요청하지 않음'을 구분할 수 있게 한다.
            List<RawBucket> raw = aggregator.aggregate(form, dimension);
            results.add(toResult(dimension, raw, totalApplicants));
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
     * 재식별 방지 최소 공개 기준(k)을 적용해, 인원이 적은 버킷의 수치를 가립니다.
     * <p>
     * {@code k <= 1}이면 마스킹 없이 원본을 그대로 반환한다(관리자 경로와 동일한 무마스킹). totalApplicants는
     * 분포가 아니므로 가리지 않는다(전체 blackout은 적용하지 않는다). 모든 dimension에 동일하게 적용한다.
     */
    private StatisticsResponseDto maskSmallBuckets(StatisticsResponseDto res, int k) {
        if (k <= 1) {
            return res;
        }

        List<StatisticsResponseDto.DimensionResult> masked = res.results().stream()
                .map(dr -> new StatisticsResponseDto.DimensionResult(
                        dr.dimension(),
                        dr.type(),
                        dr.buckets().stream().map(b -> maskBucket(b, k)).toList()
                ))
                .toList();

        return new StatisticsResponseDto(
                res.clubApplyFormId(),
                res.totalApplicants(),
                res.snapshot(),
                res.calculatedAt(),
                masked
        );
    }

    /**
     * 버킷 하나를 마스킹합니다. 인원이 {@code 0 < count < k}이면 count와 ratio를 null로 가려 코호트 규모를
     * 노출하지 않는다. count가 0인 버킷(예: 시계열 zero-fill)은 식별 대상이 없으므로 그대로 둔다.
     */
    private StatisticsResponseDto.Bucket maskBucket(StatisticsResponseDto.Bucket b, int k) {
        Long count = b.count();
        if (count != null && count > 0 && count < k) {
            return new StatisticsResponseDto.Bucket(b.key(), b.label(), null, null);
        }
        return b;
    }

    /**
     * 원본 버킷을 응답 버킷으로 변환하고 비율을 채웁니다.
     * <p>
     * 시계열은 전체 대비 비율이 의미가 없으므로 ratio를 채우지 않는다.
     */
    private StatisticsResponseDto.DimensionResult toResult(
            StatisticsDimension dimension,
            List<RawBucket> raw,
            long totalApplicants
    ) {
        boolean withRatio = dimension.getType() != DimensionType.TIME_SERIES;

        List<StatisticsResponseDto.Bucket> buckets = raw.stream()
                .map(b -> new StatisticsResponseDto.Bucket(
                        b.key(),
                        b.label(),
                        b.count(),
                        withRatio ? ratio(b.count(), totalApplicants) : null
                ))
                .toList();

        return new StatisticsResponseDto.DimensionResult(
                dimension,
                dimension.getType(),
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
