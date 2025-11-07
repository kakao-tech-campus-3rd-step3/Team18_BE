package com.kakaotech.team18.backend_server.domain.clubReview.service;

import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewRequestDto;
import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewResponseDto;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;

public interface ClubReviewService {
    SuccessResponseDto createClubReview(Long clubId, ClubReviewRequestDto request);

    ClubReviewResponseDto getClubReview(Long clubId);
}
