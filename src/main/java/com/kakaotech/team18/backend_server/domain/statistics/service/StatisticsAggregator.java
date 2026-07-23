package com.kakaotech.team18.backend_server.domain.statistics.service;

import static com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsServiceImpl.KST;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatus;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.DimensionAggregation;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository;
import com.kakaotech.team18.backend_server.domain.statistics.util.AdmissionYearBucketer;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

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

    /** 일자별 추이가 만들어낼 수 있는 최대 버킷 수. 모집 기간이 잘못 설정돼도 응답이 폭주하지 않게 한다. */
    private static final int MAX_SERIES_DAYS = 366;

    /** 학과 통계 해석 시 반드시 함께 노출해야 하는 주의 문구. */
    public static final String DEPARTMENT_NOTICE =
            "학과는 자유 입력이라 같은 학과가 표기 차이로 나뉘어 집계될 수 있습니다. 순위와 비율은 참고용으로만 사용하세요.";

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
     * @return 정렬까지 마친 원본 집계 결과
     */
    public DimensionAggregation aggregate(ClubApplyForm form, StatisticsDimension dimension) {
        return switch (dimension) {
            case GENDER -> DimensionAggregation.of(aggregateGender(form.getId()));
            case ADMISSION_YEAR -> DimensionAggregation.of(aggregateAdmissionYear(form.getId()));
            case DEPARTMENT -> aggregateDepartment(form.getId());
            case DAILY_APPLICATIONS -> aggregateDailyApplications(form);
            case TODAY_HOURLY_APPLICATIONS -> aggregateTodayHourlyApplications(form);
        };
    }

    /**
     * 일자별 지원 추이를 집계합니다.
     * <p>
     * 모집 시작일부터 마감일까지 <strong>지원자가 없는 날도 0으로 채운다.</strong> 빠진 날짜를 프론트가 알아서
     * 메우게 하면 클라이언트마다 그래프 모양이 달라진다.
     * <p>
     * <strong>마감일 이후 접수 건도 버리지 않고 그대로 집계한다.</strong> 버리면 시계열 합계가
     * {@code totalApplicants}와 어긋나 어느 쪽이 맞는지 알 수 없게 된다. 마감 이후 접수가 있다면 그 사실이
     * 그래프에 드러나는 편이 낫다.
     */
    private DimensionAggregation aggregateDailyApplications(ClubApplyForm form) {
        List<LocalDateTime> createdAtList = statisticsRepository.findCreatedAtList(form.getId());

        Map<LocalDate, Long> byDate = new HashMap<>();
        for (LocalDateTime createdAt : createdAtList) {
            byDate.merge(toKstDate(createdAt), 1L, Long::sum);
        }

        LocalDate from = resolveSeriesStart(form, byDate);
        LocalDate to = resolveSeriesEnd(form, byDate);
        if (from == null || to == null || from.isAfter(to)) {
            return DimensionAggregation.empty();
        }

        // 모집 기간 설정이 잘못돼 범위가 비정상적으로 넓어도 응답이 폭주하지 않게 상한을 둔다.
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        boolean truncated = false;
        if (days > MAX_SERIES_DAYS) {
            log.warn("지원 추이 구간이 상한을 초과해 잘라냅니다. clubApplyFormId={}, from={}, to={}, days={}",
                    form.getId(), from, to, days);
            from = to.minusDays(MAX_SERIES_DAYS - 1L);
            truncated = true;
        }

        List<RawBucket> buckets = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            buckets.add(RawBucket.of(
                    date.toString(),
                    String.format("%d월 %d일", date.getMonthValue(), date.getDayOfMonth()),
                    byDate.getOrDefault(date, 0L)));
        }

        return new DimensionAggregation(buckets, truncated ? true : null, null);
    }

    /**
     * 모집 진행 중 당일의 시간대별 지원 건수를 집계합니다.
     * <p>
     * 모집 기간이 아니면 빈 결과를 반환한다. 마감 직전 몰림을 보여주기 위한 데이터라, 모집이 끝난 뒤에는
     * 일자별 추이만으로 충분하다.
     */
    private DimensionAggregation aggregateTodayHourlyApplications(ClubApplyForm form) {
        Club club = form.getClub();
        if (RecruitStatusCalculator.calculate(club.getRecruitStart(), club.getRecruitEnd())
                != RecruitStatus.RECRUITING) {
            return DimensionAggregation.empty();
        }

        LocalDate today = LocalDate.now(KST);
        Map<Integer, Long> byHour = new HashMap<>();
        for (LocalDateTime createdAt : statisticsRepository.findCreatedAtList(form.getId())) {
            if (toKstDate(createdAt).equals(today)) {
                byHour.merge(createdAt.getHour(), 1L, Long::sum);
            }
        }

        List<RawBucket> buckets = new ArrayList<>();
        for (int hour = 0; hour <= LocalTime.now(KST).getHour(); hour++) {
            buckets.add(RawBucket.of(
                    String.format("%s %02d", today, hour),
                    hour + "시",
                    byHour.getOrDefault(hour, 0L)));
        }
        return DimensionAggregation.of(buckets);
    }

    /**
     * 시계열의 시작일. 모집 시작일을 쓰되, 없으면 가장 이른 지원일로 대체한다.
     */
    private LocalDate resolveSeriesStart(ClubApplyForm form, Map<LocalDate, Long> byDate) {
        LocalDateTime recruitStart = form.getClub().getRecruitStart();
        if (recruitStart != null) {
            return recruitStart.toLocalDate();
        }
        // 모집 기간이 설정되지 않은 지원폼(RecruitStatus.NOT_SCHEDULED)도 접수 자체는 가능하므로,
        // 실제 지원 기록이 있으면 그 범위만이라도 보여준다.
        return byDate.keySet().stream().min(LocalDate::compareTo).orElse(null);
    }

    /**
     * 시계열의 종료일. 마감일과 마지막 지원일 중 더 늦은 쪽을 쓴다.
     */
    private LocalDate resolveSeriesEnd(ClubApplyForm form, Map<LocalDate, Long> byDate) {
        LocalDate lastApplied = byDate.keySet().stream().max(LocalDate::compareTo).orElse(null);
        LocalDateTime recruitEnd = form.getClub().getRecruitEnd();
        if (recruitEnd == null) {
            return lastApplied;
        }
        LocalDate end = recruitEnd.toLocalDate();
        return (lastApplied != null && lastApplied.isAfter(end)) ? lastApplied : end;
    }

    /**
     * 접수 시각의 일자를 구합니다.
     * <p>
     * {@code createdAt}은 시간대 정보가 없는 {@code LocalDateTime}이고, 운영 DB 연결이
     * {@code serverTimezone=Asia/Seoul}로 설정되어 있어 이미 KST 기준 값이다. 따라서 여기서 시간대를 다시
     * 변환하면 오히려 어긋난다. 서버·DB 시간대가 바뀌면 이 가정이 깨지므로 배포 환경 변경 시 확인이 필요하다.
     */
    private LocalDate toKstDate(LocalDateTime createdAt) {
        return createdAt.toLocalDate();
    }

    /**
     * 학과 분포를 집계합니다.
     * <p>
     * 학과는 자유 입력이라 값 종류가 많고, 표기 차이({@code 컴퓨터공학과}/{@code 컴퓨터공학부}/{@code 컴공})가
     * 서로 다른 버킷이 된다. 상위 N개만 노출하고 나머지는 '기타'로 합치며, 파편화 때문에 순위와 비율이 실제
     * 분포와 다를 수 있다는 점을 응답에 명시한다.
     * <p>
     * 사용자가 입력한 문자열이 그대로 공개되므로 <strong>출력 이스케이프와 길이 제한</strong>을 적용한다.
     */
    private DimensionAggregation aggregateDepartment(Long clubApplyFormId) {
        List<ApplicationStatisticsRepository.DepartmentCount> rows =
                statisticsRepository.aggregateDepartment(clubApplyFormId);

        int topN = properties.department().topN();
        int maxLength = properties.department().maxLabelLength();

        List<RawBucket> buckets = new ArrayList<>();
        long othersCount = 0;
        int othersDistinct = 0;

        for (ApplicationStatisticsRepository.DepartmentCount row : rows) {
            if (buckets.size() < topN) {
                String safe = sanitizeDepartment(row.getDepartment(), maxLength);
                buckets.add(RawBucket.of(safe, safe, row.getCount()));
                continue;
            }
            othersCount += row.getCount();
            othersDistinct++;
        }

        boolean truncated = othersDistinct > 0;
        if (truncated) {
            buckets.add(new RawBucket(
                    StatisticsMasker.OTHERS_KEY, StatisticsMasker.OTHERS_LABEL, othersCount, othersDistinct));
        }

        return new DimensionAggregation(buckets, truncated ? true : null, DEPARTMENT_NOTICE);
    }

    /**
     * 공개 응답에 나갈 학과 문자열을 안전하게 만듭니다.
     * <p>
     * 입력 단계에서 길이와 허용 문자를 이미 제한하지만, 그 제한이 생기기 전에 저장된 값이 남아 있을 수 있다.
     * 출력 시점에도 한 번 더 막는다.
     */
    private String sanitizeDepartment(String raw, int maxLength) {
        String value = (raw == null || raw.isBlank()) ? UNKNOWN_LABEL : raw;
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }
        return HtmlUtils.htmlEscape(value);
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
