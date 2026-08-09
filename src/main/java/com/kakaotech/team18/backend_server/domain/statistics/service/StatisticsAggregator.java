package com.kakaotech.team18.backend_server.domain.statistics.service;

import static com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsServiceImpl.KST;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository;
import com.kakaotech.team18.backend_server.domain.statistics.util.AdmissionYearBucketer;
import com.kakaotech.team18.backend_server.domain.user.entity.Faculty;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import java.time.Year;
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

    /**
     * 유효 입학연도 하한을 기준 연도로부터 몇 년 전까지 볼지. 이보다 오래된 학번(또는 오타)은 '미입력'으로 분류한다.
     * (설정값화는 후속 단계에서 StatisticsProperties 도입 시 진행)
     */
    static final int ADMISSION_YEAR_LOOKBACK = 10;

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
            case FACULTY -> aggregateFaculty(form.getId());
            case ADMISSION_YEAR -> aggregateAdmissionYear(form.getId());
            case DAILY_APPLICATIONS -> List.of();
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

    /**
     * 학부 분포를 집계합니다.
     * <p>
     * 학부가 null인 지원자(비지원 경로로 생성됐거나 미입력)는 '미입력' 버킷으로 모은다. 목록에 없는 학부는
     * 이미 {@code Faculty.ETC}(기타)로 수집되어 있어 정상 버킷으로 나온다. 버킷은 Enum 선언 순서를 따르고,
     * '미입력'은 항상 마지막에 둔다.
     */
    private List<RawBucket> aggregateFaculty(Long clubApplyFormId) {
        List<ApplicationStatisticsRepository.FacultyCount> counts =
                statisticsRepository.aggregateFaculty(clubApplyFormId);

        List<RawBucket> buckets = new ArrayList<>();
        long unknownCount = 0;

        for (ApplicationStatisticsRepository.FacultyCount row : counts) {
            Faculty faculty = row.getFaculty();
            if (faculty == null) {
                unknownCount += row.getCount();
                continue;
            }
            buckets.add(RawBucket.of(faculty.name(), faculty.getLabel(), row.getCount()));
        }

        buckets.sort(Comparator.comparingInt(b -> Faculty.valueOf(b.key()).ordinal()));

        if (unknownCount > 0) {
            buckets.add(RawBucket.of(UNKNOWN_KEY, UNKNOWN_LABEL, unknownCount));
        }
        return buckets;
    }

    /**
     * 입학연도 분포를 집계합니다. 기준 연도는 현재 연도(KST)를 사용한다.
     */
    private List<RawBucket> aggregateAdmissionYear(Long clubApplyFormId) {
        return bucketAdmissionYears(
                statisticsRepository.aggregateByStudentId(clubApplyFormId),
                Year.now(KST).getValue());
    }

    /**
     * 학번 집계 결과를 입학연도 버킷으로 변환합니다.
     * <p>
     * 기준 연도(baseYear)를 인자로 받아 테스트에서 시점을 고정할 수 있게 한다({@code RecruitStatusCalculator}
     * 패턴). 6자리 숫자가 아니거나 유효 범위를 벗어난 학번은 '미입력'으로 모은다. 버킷은 두 자리 문자열이 아니라
     * <strong>네 자리 연도</strong> 기준으로 오름차순 정렬하고, '미입력'은 항상 마지막에 둔다.
     */
    List<RawBucket> bucketAdmissionYears(
            List<ApplicationStatisticsRepository.StudentIdCount> rows, int baseYear) {
        int minYear = baseYear - ADMISSION_YEAR_LOOKBACK;

        Map<Integer, Long> countByYear = new TreeMap<>();
        long unknownCount = 0;

        for (ApplicationStatisticsRepository.StudentIdCount row : rows) {
            Integer year = AdmissionYearBucketer.toAdmissionYear(row.getStudentId(), baseYear, minYear);
            if (year == null) {
                unknownCount += row.getCount();
                continue;
            }
            countByYear.merge(year, row.getCount(), Long::sum);
        }

        List<RawBucket> buckets = new ArrayList<>();
        countByYear.forEach((year, count) ->
                buckets.add(RawBucket.of(String.valueOf(year), AdmissionYearBucketer.toLabel(year), count)));

        if (unknownCount > 0) {
            buckets.add(RawBucket.of(UNKNOWN_KEY, UNKNOWN_LABEL, unknownCount));
        }
        return buckets;
    }
}
