package com.kakaotech.team18.backend_server.domain.clubReview.repository;

import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubReviewRepository extends JpaRepository<ClubReview, Long> {

    List<ClubReview> findByClubId(Long clubId);
}
