package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.dto.RawBucket;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository.FacultyCount;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository.GenderCount;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository.StudentIdCount;
import com.kakaotech.team18.backend_server.domain.user.entity.Faculty;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StatisticsAggregator - 성별·학부 집계")
class StatisticsAggregatorTest {

    private final ApplicationStatisticsRepository repository = mock(ApplicationStatisticsRepository.class);
    private final StatisticsAggregator aggregator = new StatisticsAggregator(repository);

    // 주의: mock을 when().thenReturn() 인자 안에서 만들면 stubbing이 중첩되어
    // UnfinishedStubbingException이 난다. 반드시 먼저 만들어 변수에 담는다.
    private GenderCount genderCount(Gender gender, long count) {
        GenderCount gc = mock(GenderCount.class);
        when(gc.getGender()).thenReturn(gender);
        when(gc.getCount()).thenReturn(count);
        return gc;
    }

    private FacultyCount facultyCount(Faculty faculty, long count) {
        FacultyCount fc = mock(FacultyCount.class);
        when(fc.getFaculty()).thenReturn(faculty);
        when(fc.getCount()).thenReturn(count);
        return fc;
    }

    private StudentIdCount studentIdCount(String studentId, long count) {
        StudentIdCount sc = mock(StudentIdCount.class);
        when(sc.getStudentId()).thenReturn(studentId);
        when(sc.getCount()).thenReturn(count);
        return sc;
    }

    private ClubApplyForm form(long id) {
        ClubApplyForm form = mock(ClubApplyForm.class);
        when(form.getId()).thenReturn(id);
        return form;
    }

    @Test
    @DisplayName("MALE/FEMALE는 enum 선언 순서로, null은 '미입력' 버킷으로 항상 마지막에 둔다")
    void aggregateGender_unknownLast() {
        GenderCount female = genderCount(Gender.FEMALE, 91);
        GenderCount unknown = genderCount(null, 2);
        GenderCount male = genderCount(Gender.MALE, 121);
        when(repository.aggregateGender(1L)).thenReturn(List.of(female, unknown, male));

        List<RawBucket> buckets = aggregator.aggregate(form(1L), StatisticsDimension.GENDER);

        assertThat(buckets).extracting(RawBucket::key).containsExactly("MALE", "FEMALE", "UNKNOWN");
        assertThat(buckets).extracting(RawBucket::count).containsExactly(121L, 91L, 2L);
        assertThat(buckets.get(2).label()).isEqualTo("미입력");
    }

    @Test
    @DisplayName("미입력이 없으면 '미입력' 버킷도 없다")
    void aggregateGender_noUnknownWhenAllPresent() {
        GenderCount male = genderCount(Gender.MALE, 10);
        GenderCount female = genderCount(Gender.FEMALE, 5);
        when(repository.aggregateGender(1L)).thenReturn(List.of(male, female));

        List<RawBucket> buckets = aggregator.aggregate(form(1L), StatisticsDimension.GENDER);

        assertThat(buckets).extracting(RawBucket::key).containsExactly("MALE", "FEMALE");
    }

    @Test
    @DisplayName("학부는 enum 선언 순서로, ETC(기타)는 정상 버킷, null은 '미입력'으로 마지막에 둔다")
    void aggregateFaculty_ordersAndUnknownLast() {
        FacultyCount etc = facultyCount(Faculty.ETC, 20);
        FacultyCount unknown = facultyCount(null, 3);
        FacultyCount engineering = facultyCount(Faculty.ENGINEERING, 74);
        FacultyCount natural = facultyCount(Faculty.NATURAL_SCIENCES, 37);
        when(repository.aggregateFaculty(1L)).thenReturn(List.of(etc, unknown, engineering, natural));

        List<RawBucket> buckets = aggregator.aggregate(form(1L), StatisticsDimension.FACULTY);

        assertThat(buckets).extracting(RawBucket::key)
                .containsExactly("ENGINEERING", "NATURAL_SCIENCES", "ETC", "UNKNOWN");
        assertThat(buckets).extracting(RawBucket::count)
                .containsExactly(74L, 37L, 20L, 3L);
        assertThat(buckets.get(2).label()).isEqualTo("기타");
        assertThat(buckets.get(3).label()).isEqualTo("미입력");
    }

    @Test
    @DisplayName("입학연도는 4자리 연도 오름차순, 6자리 아님/범위 밖 학번은 '미입력'으로 마지막에 둔다")
    void bucketAdmissionYears_ordersByYearUnknownLast() {
        // baseYear=2026, minYear=2016 (lookback 10)
        StudentIdCount y23a = studentIdCount("230001", 1);
        StudentIdCount y23b = studentIdCount("230002", 1);
        StudentIdCount y22 = studentIdCount("220001", 1);
        StudentIdCount invalid = studentIdCount("abc", 1);   // 6자리 숫자 아님 → 미입력
        StudentIdCount tooOld = studentIdCount("120001", 1); // 2012, 하한(2016) 밖 → 미입력

        List<RawBucket> buckets = aggregator.bucketAdmissionYears(
                List.of(y23a, y23b, y22, invalid, tooOld), 2026);

        assertThat(buckets).extracting(RawBucket::key).containsExactly("2022", "2023", "UNKNOWN");
        assertThat(buckets).extracting(RawBucket::count).containsExactly(1L, 2L, 2L);
        assertThat(buckets.get(0).label()).isEqualTo("22학번");
        assertThat(buckets.get(2).label()).isEqualTo("미입력");
    }
}
