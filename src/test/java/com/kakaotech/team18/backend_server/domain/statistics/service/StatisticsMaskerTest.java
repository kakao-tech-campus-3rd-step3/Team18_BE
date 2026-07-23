package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("공개 통계 재식별 방지 마스킹")
class StatisticsMaskerTest {

    private static final int THRESHOLD = 5;
    private static final int MIN_PUBLIC_TOTAL = 10;

    private StatisticsMasker masker;

    @BeforeEach
    void setUp() {
        masker = new StatisticsMasker(properties(THRESHOLD, MIN_PUBLIC_TOTAL));
    }

    private StatisticsProperties properties(int threshold, int minPublicTotal) {
        return new StatisticsProperties(
                new StatisticsProperties.Masking(threshold, minPublicTotal),
                new StatisticsProperties.Department(5, 30),
                new StatisticsProperties.AdmissionYear(1990),
                new StatisticsProperties.Precompute(true, 1, Duration.ofHours(2), Duration.ofMinutes(5))
        );
    }

    @Nested
    @DisplayName("최소 공개 기준")
    class MinPublicTotal {

        @DisplayName("누적 지원자가 기준 미만이면 분포를 공개하지 않는다")
        @Test
        void withholdsBelowThreshold() {
            assertThat(masker.isPubliclyDisclosable(0)).isFalse();
            assertThat(masker.isPubliclyDisclosable(1)).isFalse();
            assertThat(masker.isPubliclyDisclosable(MIN_PUBLIC_TOTAL - 1)).isFalse();
        }

        @DisplayName("기준에 도달하면 공개한다")
        @Test
        void disclosesAtThreshold() {
            assertThat(masker.isPubliclyDisclosable(MIN_PUBLIC_TOTAL)).isTrue();
            assertThat(masker.isPubliclyDisclosable(MIN_PUBLIC_TOTAL + 1)).isTrue();
        }
    }

    @Nested
    @DisplayName("소수 버킷 병합")
    class SmallBuckets {

        @DisplayName("임계값 미만 버킷은 개별 노출하지 않고 '기타'로 합친다")
        @Test
        void mergesBucketsBelowThreshold() {
            List<RawBucket> masked = masker.maskSmallBuckets(List.of(
                    RawBucket.of("A", "A", 30),
                    RawBucket.of("B", "B", 10),
                    RawBucket.of("C", "C", 4),
                    RawBucket.of("D", "D", 1)
            ), DimensionType.CATEGORICAL);

            assertThat(masked).extracting(RawBucket::key).containsExactly("A", "B", StatisticsMasker.OTHERS_KEY);
            assertThat(masked.get(2).count()).isEqualTo(5);
            assertThat(masked.get(2).distinctValues()).isEqualTo(2);
        }

        @DisplayName("임계값과 같은 버킷은 그대로 노출한다")
        @Test
        void keepsBucketAtThreshold() {
            List<RawBucket> masked = masker.maskSmallBuckets(List.of(
                    RawBucket.of("A", "A", 10),
                    RawBucket.of("B", "B", THRESHOLD)
            ), DimensionType.CATEGORICAL);

            assertThat(masked).extracting(RawBucket::key).containsExactly("A", "B");
        }

        @DisplayName("합칠 버킷이 없으면 '기타'를 만들지 않는다")
        @Test
        void doesNotCreateOthersWhenNothingMerged() {
            List<RawBucket> masked = masker.maskSmallBuckets(List.of(
                    RawBucket.of("A", "A", 10),
                    RawBucket.of("B", "B", 8)
            ), DimensionType.CATEGORICAL);

            assertThat(masked).extracting(RawBucket::key).doesNotContain(StatisticsMasker.OTHERS_KEY);
        }

        @DisplayName("'기타'는 항상 마지막에 온다")
        @Test
        void othersGoesLast() {
            List<RawBucket> masked = masker.maskSmallBuckets(List.of(
                    RawBucket.of("small", "small", 1),
                    RawBucket.of("big", "big", 50)
            ), DimensionType.CATEGORICAL);

            assertThat(masked.get(masked.size() - 1).key()).isEqualTo(StatisticsMasker.OTHERS_KEY);
        }

        @DisplayName("이미 '기타'인 버킷을 다시 합칠 때 원래 값 종류 수를 이어받는다")
        @Test
        void carriesOverDistinctValues() {
            // 상위 N 절단으로 만들어진 '기타'(3종)가 임계값 미만이라 다시 병합되는 상황
            List<RawBucket> masked = masker.maskSmallBuckets(List.of(
                    RawBucket.of("A", "A", 20),
                    new RawBucket(StatisticsMasker.OTHERS_KEY, StatisticsMasker.OTHERS_LABEL, 3, 3),
                    RawBucket.of("B", "B", 1)
            ), DimensionType.CATEGORICAL);

            RawBucket others = masked.get(masked.size() - 1);
            assertThat(others.count()).isEqualTo(4);
            assertThat(others.distinctValues()).isEqualTo(4);
        }

        @DisplayName("시계열에는 마스킹을 적용하지 않는다")
        @Test
        void doesNotMaskTimeSeries() {
            List<RawBucket> daily = List.of(
                    RawBucket.of("2026-03-01", "3월 1일", 0),
                    RawBucket.of("2026-03-02", "3월 2일", 2),
                    RawBucket.of("2026-03-03", "3월 3일", 40)
            );

            // 지원자가 적은 날을 '기타'로 합치면 시간축이 무너져 추이 데이터가 쓸모없어진다.
            assertThat(masker.maskSmallBuckets(daily, DimensionType.TIME_SERIES)).isEqualTo(daily);
        }

        @DisplayName("빈 버킷 목록은 그대로 둔다")
        @Test
        void keepsEmptyList() {
            assertThat(masker.maskSmallBuckets(List.of(), DimensionType.CATEGORICAL)).isEmpty();
        }
    }

    @Nested
    @DisplayName("dimension 전체 비공개")
    class WithholdDimension {

        @DisplayName("마스킹 후 버킷이 하나뿐이면 dimension 전체를 비공개 처리한다")
        @Test
        void withholdsWhenSingleBucketRemains() {
            List<RawBucket> masked = masker.maskSmallBuckets(List.of(
                    RawBucket.of("MALE", "남성", 30),
                    RawBucket.of("FEMALE", "여성", 2)
            ), DimensionType.CATEGORICAL);

            // 30명 + '기타' 2명 → 버킷 2개라 공개 가능
            assertThat(masker.shouldWithholdDimension(masked)).isFalse();

            // 전원이 같은 값이면 버킷이 하나뿐이다.
            List<RawBucket> single = masker.maskSmallBuckets(
                    List.of(RawBucket.of("MALE", "남성", 30)), DimensionType.CATEGORICAL);
            assertThat(masker.shouldWithholdDimension(single)).isTrue();
        }
    }
}
