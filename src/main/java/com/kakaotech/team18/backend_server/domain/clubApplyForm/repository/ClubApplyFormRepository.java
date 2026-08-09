package com.kakaotech.team18.backend_server.domain.clubApplyForm.repository;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
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
     * 지원폼 ID로 소속 동아리 ID만 조회합니다. (인가 검사용 경량 projection)
     */
    @Query("""
            SELECT caf.club.id
            FROM ClubApplyForm caf
            WHERE caf.id = :clubApplyFormId
            """)
    Optional<Long> findClubIdByClubApplyFormId(@Param("clubApplyFormId") Long clubApplyFormId);
}
