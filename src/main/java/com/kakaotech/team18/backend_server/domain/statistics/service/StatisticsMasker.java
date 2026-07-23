package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 공개 통계의 재식별 방지 규칙을 적용합니다.
 * <p>
 * 통계가 비로그인 공개 API이므로, dimension을 늘리기 전에 이 규칙이 먼저 확정되어야 한다. 지원자가 통계를 반복
 * 조회하면 특정 지원 전후의 차이로 개별 지원자의 속성을 추정할 수 있는데, 소수 버킷을 그대로 노출하면 그 추정이
 * 코호트가 아니라 개인 단위까지 좁혀진다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsMasker {

    /** 임계값 미만 버킷을 합칠 '기타' 버킷의 코드값. */
    public static final String OTHERS_KEY = "OTHERS";

    /** '기타' 버킷의 표시 문자열. */
    public static final String OTHERS_LABEL = "기타";

    /** 마스킹 후 버킷이 하나만 남아 dimension 전체를 비공개 처리했을 때의 안내 문구. */
    public static final String WITHHELD_NOTICE = "지원자 수가 적어 재식별 방지를 위해 비공개 처리되었습니다.";

    private final StatisticsProperties properties;

    /**
     * 누적 지원자 수가 최소 공개 기준을 넘었는지 확인합니다.
     * <p>
     * 미달이면 속성 분포를 아예 반환하지 않는다. 지원자가 0명인 경우도 여기에 걸리지만, 그때는 애초에 노출할
     * 분포 자체가 없으므로 결과가 같다.
     */
    public boolean isPubliclyDisclosable(long totalApplicants) {
        return totalApplicants >= properties.masking().minPublicTotal();
    }

    /**
     * 임계값 미만 버킷을 '기타'로 합칩니다.
     * <p>
     * 시계열에는 적용하지 않는다. 지원자가 적은 날을 '기타'로 합치면 추이 그래프의 시간축이 무너져 데이터가
     * 쓸모없어지고, 날짜별 인원수만으로는 개인의 속성 조합이 드러나지 않기 때문이다.
     *
     * @param buckets 원본 버킷 (정렬이 끝난 상태)
     * @param type    dimension의 데이터 성격
     * @return 마스킹이 적용된 버킷. '기타'는 항상 마지막에 놓는다.
     */
    public List<RawBucket> maskSmallBuckets(List<RawBucket> buckets, DimensionType type) {
        if (type == DimensionType.TIME_SERIES || buckets.isEmpty()) {
            return buckets;
        }

        int threshold = properties.masking().bucketThreshold();

        List<RawBucket> kept = new ArrayList<>();
        long mergedCount = 0;
        int mergedDistinct = 0;

        for (RawBucket bucket : buckets) {
            if (bucket.count() < threshold) {
                mergedCount += bucket.count();
                // 이미 '기타'인 버킷(상위 N 절단 결과)을 다시 합칠 때는 원래 값 종류 수를 이어받는다.
                mergedDistinct += bucket.distinctValues() != null ? bucket.distinctValues() : 1;
                continue;
            }
            kept.add(bucket);
        }

        if (mergedCount == 0) {
            return kept;
        }

        kept.add(new RawBucket(OTHERS_KEY, OTHERS_LABEL, mergedCount, mergedDistinct));
        return kept;
    }

    /**
     * 마스킹 후 남은 버킷이 하나뿐인지 판단합니다.
     * <p>
     * 버킷이 하나면 "전원이 같은 값"이라는 뜻이므로, 그 dimension은 모든 지원자의 속성을 그대로 알려주는 것과
     * 같다. 이 경우 dimension 전체를 비공개 처리한다.
     */
    public boolean shouldWithholdDimension(List<RawBucket> maskedBuckets) {
        return maskedBuckets.size() == 1;
    }
}
