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
 * 지원자 속성(성별·학부·학번)은 지원서가 아니라 {@code User}에 저장되므로, 모든 집계는 지원폼으로 필터한
 * {@code Application}에 {@code User}를 조인해 수행한다.
 * <p>
 * <strong>운영진을 제외하지 않는다.</strong> 합격자는 승인 시 {@code ClubMember.role}이
 * {@code APPLICANT}에서 {@code CLUB_MEMBER}로 바뀌므로, 역할로 필터하면 합격자가 집계에서 빠져나가
 * 모집이 진행될수록 분포가 왜곡된다. 여기서는 해당 지원폼에 접수된 지원서 전체를 센다.
 * <p>
 * dimension별 집계 쿼리는 각 dimension 구현 단계에서 추가한다.
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

    /** 성별 집계 결과 projection. */
    interface GenderCount {

        Gender getGender();

        long getCount();
    }
}
