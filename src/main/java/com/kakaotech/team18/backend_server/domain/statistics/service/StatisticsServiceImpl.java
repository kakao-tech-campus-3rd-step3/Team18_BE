package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.DimensionAggregation;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    /** 마감 직전 비공개 구간에서 내보내는 안내 문구. */
    public static final String BLACKOUT_NOTICE =
            "마감 직전에는 지원 현황을 공개하지 않습니다. 마감 후 최종 결과가 공개됩니다.";

    private final ClubApplyFormRepository clubApplyFormRepository;
    private final StatisticsAggregator aggregator;
    private final StatisticsMasker masker;
    private final StatisticsCacheStore cacheStore;
    private final StatisticsSnapshotReader snapshotReader;
    private final StatisticsProperties properties;

    /** 캐시 미스 시 같은 지원폼의 집계가 동시에 여러 번 실행되지 않도록 잡는 JVM 내부 잠금. */
    private final ConcurrentMap<Long, Object> computeLocks = new ConcurrentHashMap<>();

    @Override
    public StatisticsResponseDto getStatistics(Long clubApplyFormId, List<StatisticsDimension> dimensions) {
        ClubApplyForm form = clubApplyFormRepository.findById(clubApplyFormId)
                .orElseThrow(() -> new ClubApplyFormNotFoundException("clubApplyFormId = " + clubApplyFormId));

        // 스냅샷이 있으면 그것이 확정본이다. 이 시점에는 불합격 지원서가 이미 삭제되었을 수 있어
        // 지금 다시 집계하면 합격자만 남은 왜곡된 분포가 나온다.
        Optional<StatisticsResponseDto> snapshot = snapshotReader.find(clubApplyFormId);
        if (snapshot.isPresent()) {
            return project(snapshot.get(), dimensions);
        }

        // 마감 직전에는 아무 것도 공개하지 않는다. 집계 자체를 건너뛰므로 DB도 건드리지 않는다.
        if (isWithinDeadlineBlackout(form)) {
            log.debug("마감 직전 비공개 구간이라 지원 현황을 반환하지 않습니다. clubApplyFormId={}", clubApplyFormId);
            return blackedOut(clubApplyFormId);
        }

        if (!properties.precompute().enabled()) {
            return calculate(form, dimensions);
        }

        // 정상 운영 상태에서는 스케줄러가 캐시를 채워 두므로 조회 경로에서 집계 쿼리가 실행되지 않는다.
        return cacheStore.find(clubApplyFormId)
                .map(cached -> project(cached, dimensions))
                .orElseGet(() -> computeOnCacheMiss(form, dimensions));
    }

    /**
     * 마감 직전 비공개 구간인지 판단합니다.
     * <p>
     * 마감이 임박한 시점의 지원자 수는 "지금 넣어도 승산이 없다"는 신호로 읽혀 지원 포기를 유발한다. 게다가 그
     * 구간에는 판단을 뒤집을 시간도 없다. 그래서 마감 직전 일정 시간 동안만 공개를 멈추고, 마감 후에는 최종
     * 수치를 다시 공개한다.
     * <p>
     * 모집 기간이 설정되지 않은 지원폼에는 적용할 기준 시각이 없으므로 그대로 공개한다.
     */
    private boolean isWithinDeadlineBlackout(ClubApplyForm form) {
        LocalDateTime recruitEnd = form.getClub().getRecruitEnd();
        if (recruitEnd == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime blackoutFrom =
                recruitEnd.minus(properties.disclosure().blackoutBeforeDeadline());

        // 마감 이후는 비공개 대상이 아니다. 종료된 모집의 최종 수치는 공개한다.
        return !now.isBefore(blackoutFrom) && now.isBefore(recruitEnd);
    }

    /**
     * 비공개 구간용 응답. 지원자 수도 분포도 담지 않는다.
     * <p>
     * {@code totalApplicants}만 지우면 버킷 count의 합으로 총원이 그대로 드러나므로 분포까지 함께 감춘다.
     */
    private StatisticsResponseDto blackedOut(Long clubApplyFormId) {
        return new StatisticsResponseDto(
                clubApplyFormId, null, false, OffsetDateTime.now(KST), List.of(), BLACKOUT_NOTICE);
    }

    /**
     * 캐시 미스일 때 집계합니다.
     * <p>
     * 배포 직후나 Redis 장애처럼 캐시가 비어 있는 상황에서도 응답은 나가야 한다. 다만 이때 요청이 몰리면
     * 동시에 같은 집계가 여러 번 실행되므로(cache stampede), <strong>지원폼 단위로 한 번만 계산되도록
     * 묶는다.</strong> 먼저 진입한 스레드가 계산해 캐시에 넣고, 뒤따라온 스레드는 그 결과를 읽는다.
     * <p>
     * 이 잠금은 JVM 내부용이다. 인스턴스 간 중복까지 막는 선점 잠금은 스케줄러 경로에 있다. 캐시가 비는
     * 상황 자체가 드물어, 여기서 인스턴스 수만큼의 집계가 한 번 더 일어나는 것은 감수한다.
     */
    private StatisticsResponseDto computeOnCacheMiss(ClubApplyForm form, List<StatisticsDimension> dimensions) {
        Object lock = computeLocks.computeIfAbsent(form.getId(), id -> new Object());
        synchronized (lock) {
            try {
                // 대기하는 동안 앞선 스레드가 캐시를 채웠을 수 있다.
                Optional<StatisticsResponseDto> filled = cacheStore.find(form.getId());
                if (filled.isPresent()) {
                    return project(filled.get(), dimensions);
                }

                log.info("통계 캐시 미스. 조회 경로에서 집계합니다. clubApplyFormId={}", form.getId());
                StatisticsResponseDto full = calculate(form, StatisticsDimension.defaults());
                cacheStore.put(form.getId(), full);
                return project(full, dimensions);
            } finally {
                computeLocks.remove(form.getId(), lock);
            }
        }
    }

    /**
     * 캐시는 전체 dimension을 담고 있으므로, 요청된 항목만 골라 반환합니다.
     * <p>
     * dimension 조합마다 캐시 엔트리를 따로 두면 엔트리가 조합 수만큼 늘어나고, 그만큼 사전 계산이 채워야 할
     * 대상도 늘어난다. 전체를 한 벌만 캐시하고 읽을 때 거르는 편이 단순하다.
     */
    private StatisticsResponseDto project(StatisticsResponseDto cached, List<StatisticsDimension> dimensions) {
        List<StatisticsResponseDto.DimensionResult> filtered = cached.results().stream()
                .filter(result -> dimensions.contains(result.dimension()))
                .toList();

        return new StatisticsResponseDto(
                cached.clubApplyFormId(),
                cached.totalApplicants(),
                cached.snapshot(),
                cached.calculatedAt(),
                filtered,
                cached.notice()
        );
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
                results,
                null
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
        DimensionAggregation aggregation = aggregator.aggregate(form, dimension);
        List<RawBucket> masked = masker.maskSmallBuckets(aggregation.buckets(), dimension.getType());

        // 버킷이 하나만 남았다면 '전원이 같은 값'이라는 뜻이므로 dimension 자체를 비공개 처리한다.
        if (masker.shouldWithholdDimension(masked)) {
            return new StatisticsResponseDto.DimensionResult(
                    dimension, dimension.getType(), null, StatisticsMasker.WITHHELD_NOTICE, List.of());
        }

        return toResult(dimension, masked, totalApplicants,
                aggregation.truncated(), aggregation.notice());
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
