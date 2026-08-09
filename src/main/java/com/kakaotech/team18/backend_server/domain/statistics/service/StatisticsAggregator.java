package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * dimension별 원본 집계를 수행합니다.
 * <p>
 * 아직 구현되지 않은 dimension은 빈 버킷을 반환하며, 각 dimension은 후속 단계에서 구현한다.
 * <p>
 * 마스킹·상위 N 절단·비율 계산은 하지 않는다. 그 처리는 {@code StatisticsServiceImpl}이 담당한다.
 * dimension마다 별도 쿼리로 처리해 교차 집계로 인한 조합 폭발을 피한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsAggregator {

    /** 값이 없는 버킷의 코드값. */
    public static final String UNKNOWN_KEY = "UNKNOWN";

    /** 값이 없는 버킷의 표시 문자열. */
    public static final String UNKNOWN_LABEL = "미입력";

    private final ApplicationStatisticsRepository statisticsRepository;

    /**
     * 지원폼의 누적 지원자 수를 조회합니다.
     */
    public long countApplicants(Long clubApplyFormId) {
        return statisticsRepository.countByClubApplyFormId(clubApplyFormId);
    }

    /**
     * 요청된 dimension의 원본 버킷을 계산합니다.
     *
     * @param form      집계 대상 지원폼
     * @param dimension 집계 항목
     * @return 정렬까지 마친 원본 버킷 목록
     */
    public List<RawBucket> aggregate(ClubApplyForm form, StatisticsDimension dimension) {
        return switch (dimension) {
            case GENDER -> aggregateGender(form.getId());
            case ADMISSION_YEAR, FACULTY, DAILY_APPLICATIONS -> List.of();
        };
    }

    /**
     * 성별 분포를 집계합니다.
     * <p>
     * 성별이 null인 지원자(성별 수집 이전에 접수됐거나 미입력)는 '미입력' 버킷으로 모은다. 버킷은 Enum 선언
     * 순서를 따르고, '미입력'은 항상 마지막에 둔다.
     */
    private List<RawBucket> aggregateGender(Long clubApplyFormId) {
        List<ApplicationStatisticsRepository.GenderCount> counts =
                statisticsRepository.aggregateGender(clubApplyFormId);

        List<RawBucket> buckets = new ArrayList<>();
        long unknownCount = 0;

        for (ApplicationStatisticsRepository.GenderCount row : counts) {
            Gender gender = row.getGender();
            if (gender == null) {
                unknownCount += row.getCount();
                continue;
            }
            buckets.add(RawBucket.of(gender.name(), gender.getLabel(), row.getCount()));
        }

        buckets.sort(Comparator.comparingInt(b -> Gender.valueOf(b.key()).ordinal()));

        if (unknownCount > 0) {
            buckets.add(RawBucket.of(UNKNOWN_KEY, UNKNOWN_LABEL, unknownCount));
        }
        return buckets;
    }
}
