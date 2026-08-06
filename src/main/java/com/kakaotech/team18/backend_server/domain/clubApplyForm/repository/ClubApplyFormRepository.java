package com.kakaotech.team18.backend_server.domain.clubApplyForm.repository;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubApplyFormRepository extends JpaRepository<ClubApplyForm,Long> {

    Optional<ClubApplyForm> findByClubId(Long clubId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select form from ClubApplyForm form where form.club.id = :clubId")
    Optional<ClubApplyForm> findByClubIdForUpdate(@Param("clubId") Long clubId);

    Optional<ClubApplyForm> findByTitle(String s);
}
