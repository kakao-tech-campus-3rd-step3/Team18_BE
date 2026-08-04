package com.kakaotech.team18.backend_server.domain.clubPopularity.repository;

import com.kakaotech.team18.backend_server.domain.clubPopularity.entity.ClubView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ClubViewRepository extends JpaRepository<ClubView, Long>, ClubViewBatchRepository {

    List<ClubView> findTop500ByIdGreaterThanAndLastViewedAtAfterOrderByIdAsc(Long id, Instant cutoff);
}
