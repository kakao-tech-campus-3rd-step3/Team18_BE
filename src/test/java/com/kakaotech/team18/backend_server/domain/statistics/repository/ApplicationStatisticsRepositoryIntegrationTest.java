package com.kakaotech.team18.backend_server.domain.statistics.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository.FacultyCount;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository.GenderCount;
import com.kakaotech.team18.backend_server.domain.user.entity.Faculty;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * 통계 집계 쿼리를 실제 스키마에 대해 검증한다. 집계기 단위 테스트는 리포지토리를 목킹하므로 JPQL의
 * 조인·GROUP BY·null 그룹·projection 매핑이 실제로 동작하는지는 이 테스트가 확인한다.
 */
@DataJpaTest
@DisplayName("ApplicationStatisticsRepository - 집계 쿼리")
class ApplicationStatisticsRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApplicationStatisticsRepository repository;

    private ClubApplyForm form;

    @BeforeEach
    void setUp() {
        form = persistForm("메인 동아리");

        // 대상 폼: 성별/학부 조합 (일부는 null=미입력)
        apply(form, Gender.MALE, Faculty.ENGINEERING, 1);
        apply(form, Gender.MALE, Faculty.NATURAL_SCIENCES, 2);
        apply(form, Gender.FEMALE, Faculty.ENGINEERING, 3);
        apply(form, null, null, 4);
        apply(form, Gender.FEMALE, Faculty.ETC, 5);

        // 다른 폼: WHERE 필터가 이 지원서를 세지 않는지 확인용
        ClubApplyForm otherForm = persistForm("다른 동아리");
        apply(otherForm, Gender.MALE, Faculty.ENGINEERING, 99);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("누적 지원자 수는 해당 지원폼의 지원서만 센다")
    void countByClubApplyFormId_countsOnlyThisForm() {
        assertThat(repository.countByClubApplyFormId(form.getId())).isEqualTo(5L);
    }

    @Test
    @DisplayName("성별 집계: 성별별로 묶고 null도 하나의 그룹으로 반환한다")
    void aggregateGender_groupsIncludingNull() {
        Map<Gender, Long> counts = new HashMap<>();
        for (GenderCount row : repository.aggregateGender(form.getId())) {
            counts.put(row.getGender(), row.getCount());
        }

        assertThat(counts).hasSize(3)
                .containsEntry(Gender.MALE, 2L)
                .containsEntry(Gender.FEMALE, 2L)
                .containsEntry(null, 1L);
    }

    @Test
    @DisplayName("학부 집계: 학부별로 묶고(ETC 포함) null도 하나의 그룹으로 반환한다")
    void aggregateFaculty_groupsIncludingNull() {
        Map<Faculty, Long> counts = new HashMap<>();
        for (FacultyCount row : repository.aggregateFaculty(form.getId())) {
            counts.put(row.getFaculty(), row.getCount());
        }

        assertThat(counts).hasSize(4)
                .containsEntry(Faculty.ENGINEERING, 2L)
                .containsEntry(Faculty.NATURAL_SCIENCES, 1L)
                .containsEntry(Faculty.ETC, 1L)
                .containsEntry(null, 1L);
    }

    private ClubApplyForm persistForm(String clubName) {
        Club club = Club.builder()
                .name(clubName)
                .category(Category.STUDY)
                .location("Seoul")
                .shortIntroduction("intro")
                .build();
        entityManager.persist(club);

        ClubApplyForm applyForm = ClubApplyForm.builder()
                .club(club)
                .title(clubName + " 지원폼")
                .build();
        entityManager.persist(applyForm);
        return applyForm;
    }

    private void apply(ClubApplyForm applyForm, Gender gender, Faculty faculty, int seq) {
        User user = User.builder()
                .name("지원자" + seq)
                .email("applicant" + seq + "@test.com")
                .phoneNumber("010-0000-" + String.format("%04d", seq))
                .studentId(String.format("23%04d", seq))
                .department("학과")
                .gender(gender)
                .faculty(faculty)
                .build();
        entityManager.persist(user);

        Application application = Application.builder()
                .user(user)
                .clubApplyForm(applyForm)
                .build();
        entityManager.persist(application);
    }
}
