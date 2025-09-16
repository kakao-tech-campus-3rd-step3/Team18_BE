package com.kakaotech.team18.backend_server.domain.clubApplyForm.repository;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubApplyFormRepository extends JpaRepository<ClubApplyForm,Long> {
    Optional<ClubApplyForm> findByClubIdAndIsActiveTrue(Long clubId);

}
