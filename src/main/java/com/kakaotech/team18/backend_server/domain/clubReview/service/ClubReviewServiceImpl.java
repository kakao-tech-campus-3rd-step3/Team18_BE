package com.kakaotech.team18.backend_server.domain.clubReview.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewRequestDto;
import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import com.kakaotech.team18.backend_server.domain.clubReview.repository.ClubReviewRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClubReviewServiceImpl implements ClubReviewService {

    private final ClubReviewRepository clubReviewRepository;
    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public SuccessResponseDto createClubReview(Long clubId, ClubReviewRequestDto request) {
        Club findClub = findClub(clubId);
        ClubReview clubReview = ClubReview.builder()
                .club(findClub)
                .content(request.content())
                .writer(request.studentId())
                .build();
        clubReviewRepository.save(clubReview);
        log.info("ClubReview created for clubId: {}, reviewId: {}", clubId, clubReview.getId());
        return new SuccessResponseDto(true);
    }

    private Club findClub(Long clubId) {
        return clubRepository.findById(clubId).orElseThrow(() -> {
            log.warn("Club not found for clubId: {}", clubId);
            return new ClubNotFoundException("clubId = " + clubId);
        });
    }
}
