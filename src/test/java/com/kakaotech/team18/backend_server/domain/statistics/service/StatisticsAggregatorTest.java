package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.config.StatisticsProperties;
import com.kakaotech.team18.backend_server.domain.statistics.dto.DimensionAggregation;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("dimension별 원본 집계")
class StatisticsAggregatorTest {

    private static final Long FORM_ID = 12L;

    @Mock
    ApplicationStatisticsRepository repository;

    ClubApplyForm form;
    Club club;
    StatisticsAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new StatisticsAggregator(repository, properties());

        club = mock(Club.class);
        form = mock(ClubApplyForm.class);
        lenient().when(form.getId()).thenReturn(FORM_ID);
        lenient().when(form.getClub()).thenReturn(club);
    }

    private StatisticsProperties properties() {
        return new StatisticsProperties(
                new StatisticsProperties.Masking(5, 10),
                new StatisticsProperties.Department(3, 10),
                new StatisticsProperties.AdmissionYear(1990),
                new StatisticsProperties.Disclosure(Duration.ofMinutes(5)),
                new StatisticsProperties.Precompute(true, 1, Duration.ofHours(2), Duration.ofMinutes(5))
        );
    }

    /**
     * projection을 Mockito로 만들면 {@code when(...)} 안에서 다시 {@code when(...)}이 불려
     * UnfinishedStubbingException이 난다. 값만 담으면 되는 인터페이스라 직접 구현한다.
     */
    private ApplicationStatisticsRepository.GenderCount genderRow(Gender gender, long count) {
        return new ApplicationStatisticsRepository.GenderCount() {
            @Override
            public Gender getGender() {
                return gender;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    private ApplicationStatisticsRepository.DepartmentCount departmentRow(String name, long count) {
        return new ApplicationStatisticsRepository.DepartmentCount() {
            @Override
            public String getDepartment() {
                return name;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    @Nested
    @DisplayName("성별")
    class GenderDimension {

        @DisplayName("성별이 없는 지원자는 '미입력' 버킷으로 모이고 항상 마지막에 온다")
        @Test
        void nullGenderBecomesUnknownBucketAtEnd() {
            when(repository.aggregateGender(FORM_ID)).thenReturn(List.of(
                    genderRow(null, 2),
                    genderRow(Gender.FEMALE, 91),
                    genderRow(Gender.MALE, 121)
            ));

            List<RawBucket> buckets = aggregator.aggregate(form, StatisticsDimension.GENDER).buckets();

            assertThat(buckets).extracting(RawBucket::key)
                    .containsExactly("MALE", "FEMALE", StatisticsAggregator.UNKNOWN_KEY);
            assertThat(buckets).extracting(RawBucket::label)
                    .containsExactly("남성", "여성", "미입력");
            assertThat(buckets.get(2).count()).isEqualTo(2);
        }

        @DisplayName("성별 데이터가 전혀 없던 과거 지원폼도 집계된다")
        @Test
        void aggregatesFormWithNoGenderData() {
            when(repository.aggregateGender(FORM_ID)).thenReturn(List.of(genderRow(null, 40)));

            List<RawBucket> buckets = aggregator.aggregate(form, StatisticsDimension.GENDER).buckets();

            assertThat(buckets).hasSize(1);
            assertThat(buckets.get(0).key()).isEqualTo(StatisticsAggregator.UNKNOWN_KEY);
            assertThat(buckets.get(0).count()).isEqualTo(40);
        }

        @DisplayName("지원자가 없으면 빈 버킷이다")
        @Test
        void emptyWhenNoApplicants() {
            when(repository.aggregateGender(FORM_ID)).thenReturn(List.of());

            assertThat(aggregator.aggregate(form, StatisticsDimension.GENDER).buckets()).isEmpty();
        }
    }

    @Nested
    @DisplayName("학번")
    class AdmissionYearDimension {

        @DisplayName("네 자리 연도 기준으로 정렬하고 '미입력'은 마지막에 둔다")
        @Test
        void sortsByFourDigitYear() {
            int thisYear = LocalDate.now().getYear();
            String recent = String.format("%02d0001", thisYear % 100);

            when(repository.findStudentIds(FORM_ID)).thenReturn(List.of(
                    "991234",   // 1999
                    recent,     // 올해
                    "050001",   // 2005
                    "abc",      // 미입력
                    "12345"     // 미입력 (5자리)
            ));

            List<RawBucket> buckets =
                    aggregator.aggregate(form, StatisticsDimension.ADMISSION_YEAR).buckets();

            assertThat(buckets).extracting(RawBucket::key)
                    .containsExactly("1999", "2005", String.valueOf(thisYear),
                            StatisticsAggregator.UNKNOWN_KEY);
            assertThat(buckets.get(buckets.size() - 1).count()).isEqualTo(2);
        }

        @DisplayName("같은 연도의 학번은 하나의 버킷으로 합쳐진다")
        @Test
        void groupsSameYear() {
            when(repository.findStudentIds(FORM_ID))
                    .thenReturn(List.of("220001", "220002", "220003"));

            List<RawBucket> buckets =
                    aggregator.aggregate(form, StatisticsDimension.ADMISSION_YEAR).buckets();

            assertThat(buckets).hasSize(1);
            assertThat(buckets.get(0).key()).isEqualTo("2022");
            assertThat(buckets.get(0).label()).isEqualTo("22학번");
            assertThat(buckets.get(0).count()).isEqualTo(3);
        }

        @DisplayName("6자리 학번 원본은 버킷 어디에도 남지 않는다")
        @Test
        void neverLeaksRawStudentId() {
            when(repository.findStudentIds(FORM_ID)).thenReturn(List.of("220123", "220124"));

            List<RawBucket> buckets =
                    aggregator.aggregate(form, StatisticsDimension.ADMISSION_YEAR).buckets();

            assertThat(buckets).allSatisfy(bucket -> {
                assertThat(bucket.key()).doesNotContain("220123", "220124");
                assertThat(bucket.label()).doesNotContain("220123", "220124");
            });
        }
    }

    @Nested
    @DisplayName("학과")
    class DepartmentDimension {

        @DisplayName("상위 N개만 노출하고 나머지는 '기타'로 합치며 truncated를 알린다")
        @Test
        void truncatesToTopN() {
            when(repository.aggregateDepartment(FORM_ID)).thenReturn(List.of(
                    departmentRow("컴퓨터공학과", 37),
                    departmentRow("전자공학과", 20),
                    departmentRow("기계공학과", 15),
                    departmentRow("경영학과", 8),
                    departmentRow("영문학과", 6),
                    departmentRow("사학과", 3)
            ));

            DimensionAggregation result = aggregator.aggregate(form, StatisticsDimension.DEPARTMENT);

            // topN = 3
            assertThat(result.buckets()).extracting(RawBucket::key)
                    .containsExactly("컴퓨터공학과", "전자공학과", "기계공학과", StatisticsMasker.OTHERS_KEY);
            assertThat(result.truncated()).isTrue();

            RawBucket others = result.buckets().get(3);
            assertThat(others.count()).isEqualTo(8 + 6 + 3);
            assertThat(others.distinctValues()).isEqualTo(3);
        }

        @DisplayName("상위 N 이내면 truncated를 표시하지 않는다")
        @Test
        void noTruncationWithinTopN() {
            when(repository.aggregateDepartment(FORM_ID)).thenReturn(List.of(
                    departmentRow("컴퓨터공학과", 37),
                    departmentRow("전자공학과", 20)
            ));

            DimensionAggregation result = aggregator.aggregate(form, StatisticsDimension.DEPARTMENT);

            assertThat(result.truncated()).isNull();
            assertThat(result.buckets()).extracting(RawBucket::key)
                    .doesNotContain(StatisticsMasker.OTHERS_KEY);
        }

        @DisplayName("표기 파편화 주의 문구를 항상 함께 제공한다")
        @Test
        void alwaysAttachesFragmentationNotice() {
            when(repository.aggregateDepartment(FORM_ID))
                    .thenReturn(List.of(departmentRow("컴퓨터공학과", 10)));

            assertThat(aggregator.aggregate(form, StatisticsDimension.DEPARTMENT).notice())
                    .isEqualTo(StatisticsAggregator.DEPARTMENT_NOTICE);
        }

        @DisplayName("자유 입력 문자열은 이스케이프해서 내보낸다")
        @Test
        void escapesFreeTextOutput() {
            when(repository.aggregateDepartment(FORM_ID)).thenReturn(List.of(
                    departmentRow("<script>", 10)
            ));

            RawBucket bucket = aggregator.aggregate(form, StatisticsDimension.DEPARTMENT).buckets().get(0);

            assertThat(bucket.label()).doesNotContain("<script>");
            assertThat(bucket.label()).isEqualTo("&lt;script&gt;");
            assertThat(bucket.key()).doesNotContain("<");
        }

        @DisplayName("응답에 나가는 학과 문자열의 길이를 제한한다")
        @Test
        void limitsLabelLength() {
            String tooLong = "가".repeat(50);
            when(repository.aggregateDepartment(FORM_ID)).thenReturn(List.of(departmentRow(tooLong, 10)));

            RawBucket bucket = aggregator.aggregate(form, StatisticsDimension.DEPARTMENT).buckets().get(0);

            // maxLabelLength = 10
            assertThat(bucket.label()).hasSize(10);
        }
    }

    @Nested
    @DisplayName("지원 추이")
    class DailyDimension {

        @DisplayName("지원자가 없는 날도 0으로 채우고 날짜 순서를 보장한다")
        @Test
        void fillsEmptyDaysWithZero() {
            LocalDate start = LocalDate.of(2026, 3, 1);
            when(club.getRecruitStart()).thenReturn(start.atStartOfDay());
            when(club.getRecruitEnd()).thenReturn(start.plusDays(3).atTime(23, 59));
            when(repository.findCreatedAtList(FORM_ID)).thenReturn(List.of(
                    start.atTime(10, 0),
                    start.plusDays(2).atTime(9, 0),
                    start.plusDays(2).atTime(11, 0)
            ));

            List<RawBucket> buckets =
                    aggregator.aggregate(form, StatisticsDimension.DAILY_APPLICATIONS).buckets();

            assertThat(buckets).extracting(RawBucket::key)
                    .containsExactly("2026-03-01", "2026-03-02", "2026-03-03", "2026-03-04");
            assertThat(buckets).extracting(RawBucket::count)
                    .containsExactly(1L, 0L, 2L, 0L);
            assertThat(buckets.get(0).label()).isEqualTo("3월 1일");
        }

        @DisplayName("마감일 이후 접수 건도 버리지 않아 시계열 합계가 총 지원자 수와 일치한다")
        @Test
        void keepsApplicationsAfterDeadline() {
            LocalDate start = LocalDate.of(2026, 3, 1);
            when(club.getRecruitStart()).thenReturn(start.atStartOfDay());
            when(club.getRecruitEnd()).thenReturn(start.plusDays(1).atTime(23, 59));
            when(repository.findCreatedAtList(FORM_ID)).thenReturn(List.of(
                    start.atTime(10, 0),
                    start.plusDays(3).atTime(10, 0)   // 마감 이후 접수
            ));

            List<RawBucket> buckets =
                    aggregator.aggregate(form, StatisticsDimension.DAILY_APPLICATIONS).buckets();

            assertThat(buckets).extracting(RawBucket::key).contains("2026-03-04");
            assertThat(buckets.stream().mapToLong(RawBucket::count).sum()).isEqualTo(2);
        }

        @DisplayName("모집 기간이 설정되지 않았어도 실제 지원 기록 범위로 집계한다")
        @Test
        void fallsBackToApplicationRangeWhenRecruitPeriodMissing() {
            when(club.getRecruitStart()).thenReturn(null);
            when(club.getRecruitEnd()).thenReturn(null);
            when(repository.findCreatedAtList(FORM_ID)).thenReturn(List.of(
                    LocalDateTime.of(2026, 3, 5, 10, 0),
                    LocalDateTime.of(2026, 3, 6, 10, 0)
            ));

            List<RawBucket> buckets =
                    aggregator.aggregate(form, StatisticsDimension.DAILY_APPLICATIONS).buckets();

            assertThat(buckets).extracting(RawBucket::key).containsExactly("2026-03-05", "2026-03-06");
        }

        @DisplayName("모집 기간도 지원 기록도 없으면 빈 결과다")
        @Test
        void emptyWhenNothingToShow() {
            when(club.getRecruitStart()).thenReturn(null);
            when(club.getRecruitEnd()).thenReturn(null);
            when(repository.findCreatedAtList(FORM_ID)).thenReturn(new ArrayList<>());

            assertThat(aggregator.aggregate(form, StatisticsDimension.DAILY_APPLICATIONS).buckets())
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("당일 시간대별 지원")
    class TodayHourlyDimension {

        @DisplayName("모집 기간이 아니면 빈 결과다")
        @Test
        void emptyWhenNotRecruiting() {
            when(club.getRecruitStart()).thenReturn(LocalDateTime.now().minusDays(10));
            when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().minusDays(1));

            assertThat(aggregator.aggregate(form, StatisticsDimension.TODAY_HOURLY_APPLICATIONS)
                    .buckets()).isEmpty();
        }

        @DisplayName("모집 중이면 오늘 0시부터 현재 시각까지의 버킷을 순서대로 만든다")
        @Test
        void buildsHourlyBucketsWhileRecruiting() {
            when(club.getRecruitStart()).thenReturn(LocalDateTime.now().minusDays(1));
            when(club.getRecruitEnd()).thenReturn(LocalDateTime.now().plusDays(1));
            when(repository.findCreatedAtList(anyLong())).thenReturn(List.of(LocalDateTime.now()));

            List<RawBucket> buckets = aggregator
                    .aggregate(form, StatisticsDimension.TODAY_HOURLY_APPLICATIONS).buckets();

            assertThat(buckets).isNotEmpty();
            assertThat(buckets.get(0).label()).isEqualTo("0시");
            assertThat(buckets.stream().mapToLong(RawBucket::count).sum()).isEqualTo(1);
        }
    }
}
