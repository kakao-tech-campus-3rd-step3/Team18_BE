package com.kakaotech.team18.backend_server.domain.statistics.repository;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 통계 집계 전용 조회 리포지토리.
 * <p>
 * 지원자 속성(성별·학과·학번)은 지원서가 아니라 {@code User}에 저장되므로, 모든 집계는 지원폼으로 필터한
 * {@code Application}에 {@code User}를 조인해 수행한다.
 * <p>
 * <strong>운영진을 제외하지 않는다.</strong> 합격자는 승인 시 {@code ClubMember.role}이
 * {@code APPLICANT}에서 {@code CLUB_MEMBER}로 바뀌므로, 역할로 필터하면 합격자가 집계에서 빠져나가
 * 모집이 진행될수록 분포가 왜곡된다. 여기서는 해당 지원폼에 접수된 지원서 전체를 센다.
 */
public interface ApplicationStatisticsRepository extends Repository<Application, Long> {

    /**
     * 지원폼의 누적 지원자 수.
     */
    @Query("""
            SELECT count(a.id)
            FROM Application a
            WHERE a.clubApplyForm.id = :clubApplyFormId
            """)
    long countByClubApplyFormId(@Param("clubApplyFormId") Long clubApplyFormId);

    /**
     * 성별 분포. 성별이 없는(null) 지원자도 하나의 그룹으로 반환된다.
     */
    @Query("""
            SELECT u.gender AS gender, count(a.id) AS count
            FROM Application a
            JOIN a.user u
            WHERE a.clubApplyForm.id = :clubApplyFormId
            GROUP BY u.gender
            """)
    List<GenderCount> aggregateGender(@Param("clubApplyFormId") Long clubApplyFormId);

    /**
     * 지원자들의 학번 목록.
     * <p>
     * GROUP BY로 DB에서 바로 자르지 않는 이유는, 6자리 숫자 여부와 유효 입학연도 범위를 검증해야 하는데
     * 그 규칙을 JPQL로 표현하면 DB 방언에 묶이기 때문이다. 학번은 서비스 내부에서만 쓰이며
     * <strong>응답 DTO로는 절대 나가지 않는다.</strong> 지원폼 하나의 지원자 수는 수백 규모라 메모리 부담도 없다.
     */
    @Query("""
            SELECT u.studentId
            FROM Application a
            JOIN a.user u
            WHERE a.clubApplyForm.id = :clubApplyFormId
            """)
    List<String> findStudentIds(@Param("clubApplyFormId") Long clubApplyFormId);

    /**
     * 학과 분포. 지원자가 많은 순으로 정렬해 반환하므로, 상위 N개 절단은 앞에서부터 자르면 된다.
     * <p>
     * 동점일 때 순서가 흔들리지 않도록 학과명을 2차 정렬 키로 둔다. 순서가 매 집계마다 달라지면 같은 데이터에도
     * '기타'에 들어가는 학과가 바뀌어 통계가 튀어 보인다.
     */
    @Query("""
            SELECT u.department AS department, count(a.id) AS count
            FROM Application a
            JOIN a.user u
            WHERE a.clubApplyForm.id = :clubApplyFormId
            GROUP BY u.department
            ORDER BY count(a.id) DESC, u.department ASC
            """)
    List<DepartmentCount> aggregateDepartment(@Param("clubApplyFormId") Long clubApplyFormId);

    /** 성별 집계 결과 projection. */
    interface GenderCount {

        Gender getGender();

        long getCount();
    }

    /** 학과 집계 결과 projection. */
    interface DepartmentCount {

        String getDepartment();

        long getCount();
    }
}
