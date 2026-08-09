package com.kakaotech.team18.backend_server.domain.statistics.entity;

import com.kakaotech.team18.backend_server.global.exception.exceptions.UnsupportedStatisticsDimensionException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공개 통계로 제공하는 집계 항목.
 * <p>
 * dimension마다 별도 쿼리로 집계한다. 두 항목을 교차 집계(예: 학부 x 성별)하면 버킷 수가 곱으로 늘어 소수 버킷이
 * 급증하고 재식별 위험이 커지므로, 1차 범위에서는 교차 집계를 제공하지 않는다.
 */
@Getter
@RequiredArgsConstructor
public enum StatisticsDimension {

    GENDER(DimensionType.CATEGORICAL),
    ADMISSION_YEAR(DimensionType.ORDINAL),
    FACULTY(DimensionType.CATEGORICAL),
    DAILY_APPLICATIONS(DimensionType.TIME_SERIES);

    private final DimensionType type;

    /**
     * 쿼리 파라미터로 들어온 문자열을 dimension으로 변환합니다.
     *
     * @param raw 클라이언트가 요청한 dimension 이름
     * @return 대응하는 dimension
     * @throws UnsupportedStatisticsDimensionException 정의되지 않은 이름인 경우 (400)
     */
    public static StatisticsDimension from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new UnsupportedStatisticsDimensionException("dimension 값이 비어 있습니다.");
        }
        try {
            return StatisticsDimension.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStatisticsDimensionException(
                    "지원하지 않는 dimension: " + raw + " (사용 가능: " + Arrays.toString(values()) + ")");
        }
    }

    /** 클라이언트가 dimension을 지정하지 않았을 때 사용할 기본 목록. */
    public static List<StatisticsDimension> defaults() {
        return List.of(values());
    }

    /**
     * 쿼리 파라미터 문자열 목록을 dimension 목록으로 해석합니다. 비어 있으면 전체({@link #defaults()})를,
     * 지정돼 있으면 각 값을 {@link #from(String)}으로 변환해 중복을 제거한 목록을 반환합니다.
     */
    public static List<StatisticsDimension> resolve(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return defaults();
        }
        return raw.stream()
                .map(StatisticsDimension::from)
                .distinct()
                .toList();
    }
}
