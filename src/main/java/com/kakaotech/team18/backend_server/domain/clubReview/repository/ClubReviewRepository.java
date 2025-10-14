package com.kakaotech.team18.backend_server.domain.clubReview.repository;

import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubReviewRepository extends JpaRepository<ClubReview, Long> {
}
