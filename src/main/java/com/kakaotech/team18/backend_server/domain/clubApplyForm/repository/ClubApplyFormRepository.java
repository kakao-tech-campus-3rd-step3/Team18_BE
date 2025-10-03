package com.kakaotech.team18.backend_server.domain.clubApplyForm.repository;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubApplyFormRepository extends JpaRepository<ClubApplyForm,Long> {
    Optional<ClubApplyForm> findByClubIdAndIsActiveTrue(Long clubId);

    Optional<ClubApplyForm> findByClubId(Long clubId);

    ClubApplyForm findByClub_Id(Long clubId);
}
