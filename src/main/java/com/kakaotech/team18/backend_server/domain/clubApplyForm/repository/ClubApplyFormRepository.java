package com.kakaotech.team18.backend_server.domain.clubApplyForm.repository;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubApplyFormRepository extends JpaRepository<ClubApplyForm,Long> {

    Optional<ClubApplyForm> findByClubId(Long clubId);

    Optional<ClubApplyForm> findByTitle(String s);

    /**
     * 통계 사전 계산 대상 지원폼을 조회합니다.
     * <p>
     * 모집 진행 중({@code recruitStart <= now < recruitEnd})인 지원폼과, 방금 마감된 지원폼을 함께 가져온다.
     * 마감된 지원폼을 포함하는 이유는 <strong>모집 종료 시점에는 증가분과 무관하게 최종 수치를 공개해야</strong>
     * 하기 때문이다. 진행 중인 것만 대상으로 삼으면 마감 직전 몇 건이 공개 갱신 단위에 미달해 영영 반영되지
     * 않는다.
     * <p>
     * 조건은 {@code RecruitStatusCalculator}의 RECRUITING 판정과 같은 기준이다.
     *
     * @param now         현재 시각
     * @param closedSince 이 시각 이후에 마감된 지원폼까지 포함
     */
    @Query("""
            SELECT f
            FROM ClubApplyForm f
            JOIN FETCH f.club c
            WHERE c.recruitStart IS NOT NULL
              AND c.recruitEnd IS NOT NULL
              AND c.recruitStart <= :now
              AND c.recruitEnd > :closedSince
            """)
    List<ClubApplyForm> findAllForPrecompute(
            @Param("now") LocalDateTime now,
            @Param("closedSince") LocalDateTime closedSince);
}
