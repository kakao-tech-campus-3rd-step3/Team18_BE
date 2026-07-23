package com.kakaotech.team18.backend_server.domain.statistics.dto;

import java.util.List;

/**
 * 단일 dimension의 원본 집계 결과.
 * <p>
 * 버킷뿐 아니라 "상위 N개로 잘렸는지", "해석 시 주의할 점이 있는지"까지 함께 전달한다. 이 정보는 버킷 값만
 * 봐서는 알 수 없고, 집계 과정을 아는 쪽에서만 만들 수 있다.
 *
 * @param buckets   집계·정렬이 끝난 버킷
 * @param truncated 상위 N개로 잘렸으면 true, 아니면 null (응답에서 생략됨)
 * @param notice    해석 시 주의사항, 없으면 null
 */
public record DimensionAggregation(
        List<RawBucket> buckets,
        Boolean truncated,
        String notice
) {

    public static DimensionAggregation of(List<RawBucket> buckets) {
        return new DimensionAggregation(buckets, null, null);
    }

    public static DimensionAggregation empty() {
        return new DimensionAggregation(List.of(), null, null);
    }

    public DimensionAggregation withBuckets(List<RawBucket> replaced) {
        return new DimensionAggregation(replaced, truncated, notice);
    }
}
