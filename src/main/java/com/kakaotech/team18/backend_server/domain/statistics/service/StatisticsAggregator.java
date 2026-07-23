package com.kakaotech.team18.backend_server.domain.statistics.service;

import static com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsServiceImpl.KST;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository;
import com.kakaotech.team18.backend_server.domain.statistics.util.AdmissionYearBucketer;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * dimension별 원본 집계를 수행합니다.
 * <p>
 * 마스킹·상위 N 절단·비율 계산은 하지 않는다. 그 처리는 {@code StatisticsServiceImpl}이 담당한다.
 * <p>
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
    private final StatisticsProperties properties;

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
            case ADMISSION_YEAR -> aggregateAdmissionYear(form.getId());
            case DEPARTMENT, DAILY_APPLICATIONS -> List.of();
        };
    }

    /**
     * 학번에서 입학연도를 뽑아 집계합니다.
     * <p>
     * 버킷 key는 네 자리 연도(`2022`), 라벨은 `22학번` 형식이다. <strong>정렬은 두 자리 문자열이 아니라 네 자리
     * 연도로 수행한다.</strong> 두 자리로 정렬하면 1999학번(`99`)이 2022학번(`22`) 뒤에 오는 역전이 생긴다.
     */
    private List<RawBucket> aggregateAdmissionYear(Long clubApplyFormId) {
        int baseYear = LocalDate.now(KST).getYear();
        int minYear = properties.admissionYear().minYear();

        Map<Integer, Long> byYear = new TreeMap<>();
        long unknownCount = 0;

        for (String studentId : statisticsRepository.findStudentIds(clubApplyFormId)) {
            Integer year = AdmissionYearBucketer.toAdmissionYear(studentId, baseYear, minYear);
            if (year == null) {
                unknownCount++;
                continue;
            }
            byYear.merge(year, 1L, Long::sum);
        }

        List<RawBucket> buckets = new ArrayList<>();
        byYear.forEach((year, count) ->
                buckets.add(RawBucket.of(String.valueOf(year), AdmissionYearBucketer.toLabel(year), count)));

        if (unknownCount > 0) {
            buckets.add(RawBucket.of(UNKNOWN_KEY, UNKNOWN_LABEL, unknownCount));
        }
        return buckets;
    }

    /**
     * 성별 분포를 집계합니다.
     * <p>
     * 성별이 null인 지원자(성별 수집 이전에 접수된 건)는 '미입력' 버킷으로 모은다. 버킷은 Enum 선언 순서를
     * 따르고, '미입력'은 항상 마지막에 둔다.
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
