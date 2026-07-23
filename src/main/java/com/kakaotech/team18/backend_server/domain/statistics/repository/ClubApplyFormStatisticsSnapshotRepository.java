package com.kakaotech.team18.backend_server.domain.statistics.repository;

import com.kakaotech.team18.backend_server.domain.statistics.entity.ClubApplyFormStatisticsSnapshot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubApplyFormStatisticsSnapshotRepository
        extends JpaRepository<ClubApplyFormStatisticsSnapshot, Long> {

    Optional<ClubApplyFormStatisticsSnapshot> findByClubApplyFormId(Long clubApplyFormId);

    boolean existsByClubApplyFormId(Long clubApplyFormId);
}
