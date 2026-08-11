package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.cache.StatisticsCache;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
// 최소 공개 기준 판정(지원자 수)과 dimension 집계가 같은 스냅샷을 보도록 반복 읽기로 고정한다.
// 운영 MySQL(InnoDB)은 기본이 REPEATABLE_READ지만, DB 기본값에 의존하지 않고 요구사항을 명시한다.
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class StatisticsServiceImpl implements StatisticsService {

    /** 모든 일자·시각 집계의 기준 시간대. */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 비율의 소수점 자릿수. 모든 dimension에서 동일하게 고정한다. */
    private static final int RATIO_SCALE = 3;

    private final ClubApplyFormRepository clubApplyFormRepository;
    private final StatisticsAggregator aggregator;
    private final StatisticsProperties properties;
    private final StatisticsCache cache;

    @Override
    public StatisticsResponseDto getStatistics(Long clubApplyFormId, List<StatisticsDimension> dimensions) {
        ClubApplyForm form = clubApplyFormRepository.findById(clubApplyFormId)
                .orElseThrow(() -> new ClubApplyFormNotFoundException("clubApplyFormId = " + clubApplyFormId));

        // 공개 통계는 폼별 전체 결과를 Redis에 TTL 캐시한다(스케줄러 없이 요청 시점에만 채우는 cache-aside).
        // 캐시에는 마스킹하지 않은 전체 dimension 결과를 담고, 마스킹·subset은 serve 시점에 적용한다.
        // 캐시 장애 시 find는 빈 값을 주므로 자연히 실시간 계산으로 폴백한다.
        StatisticsResponseDto full = cache.find(clubApplyFormId).orElseGet(() -> {
            StatisticsResponseDto computed = calculate(form, StatisticsDimension.defaults());
            cache.put(clubApplyFormId, computed);
            return computed;
        });

        // 재식별 방지: 전체 지원자 수가 최소 공개 기준 미만이면 분포를 비공개(masked)한다.
        // 기준은 전체 지원자 수에만 걸리고 개별 버킷에는 걸지 않는다(그래야 버킷 간 뺄셈 역산 문제가 없다).
        // 판정과 응답이 같은 캐시 스냅샷의 totalApplicants를 쓰므로 서로 어긋나지 않는다.
        if (full.totalApplicants() < properties.minTotalApplicants()) {
            return maskedResponse(clubApplyFormId, full.totalApplicants());
        }

        return filterDimensions(full, dimensions);
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
     * 공개 캐시 채움 경로와 관리자 실시간 경로가 같은 결과를 내도록 이 메서드를 공유한다. 지원자 수를 한 번만
     * 읽어 전체 수와 비율이 같은 스냅샷을 쓰도록 한다(클래스의 REPEATABLE_READ와 함께 집계 간 일관성 보장).
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
                false,
                OffsetDateTime.now(KST),
                results
        );
    }

    /**
     * 캐시된 전체 결과에서 요청한 dimension만 골라 응답을 만듭니다.
     * <p>
     * 캐시에는 항상 전체 dimension 결과가 들어 있으므로, subset 요청은 재계산 없이 여기서 걸러낸다.
     * 순서는 요청한 dimension 순서를 따른다. totalApplicants·calculatedAt 등 나머지는 캐시 값 그대로 유지한다.
     */
    private StatisticsResponseDto filterDimensions(StatisticsResponseDto full, List<StatisticsDimension> dimensions) {
        Map<StatisticsDimension, StatisticsResponseDto.DimensionResult> byDimension = full.results().stream()
                .collect(Collectors.toMap(StatisticsResponseDto.DimensionResult::dimension, Function.identity()));

        List<StatisticsResponseDto.DimensionResult> selected = dimensions.stream()
                .map(byDimension::get)
                .filter(Objects::nonNull)
                .toList();

        return new StatisticsResponseDto(
                full.clubApplyFormId(),
                full.totalApplicants(),
                full.snapshot(),
                full.masked(),
                full.calculatedAt(),
                selected
        );
    }

    /**
     * 최소 공개 기준 미달로 분포를 비공개 처리한 응답을 만듭니다.
     * <p>
     * {@code masked=true}와 빈 results로, '지원자가 적어 비공개'임을 '지원자 0명'(results가 있고 버킷 count가 0)과
     * 구분해 알린다. totalApplicants는 분포가 아니므로 그대로 노출한다.
     */
    private StatisticsResponseDto maskedResponse(Long clubApplyFormId, long totalApplicants) {
        return new StatisticsResponseDto(
                clubApplyFormId,
                totalApplicants,
                false,
                true,
                OffsetDateTime.now(KST),
                List.of()
        );
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
