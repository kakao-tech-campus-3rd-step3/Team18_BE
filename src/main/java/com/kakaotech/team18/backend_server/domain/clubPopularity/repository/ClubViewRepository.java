package com.kakaotech.team18.backend_server.domain.clubPopularity.repository;

import com.kakaotech.team18.backend_server.domain.clubPopularity.entity.ClubView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubViewRepository extends JpaRepository<ClubView, Long>, ClubViewBatchRepository {
}
