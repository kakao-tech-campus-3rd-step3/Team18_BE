package com.kakaotech.team18.backend_server.domain.clubReview.service;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewRequestDto;
import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import com.kakaotech.team18.backend_server.domain.clubReview.repository.ClubReviewRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UnRegisteredUserException;
import java.util.List;
import java.util.Optional;
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
    private final ClubMemberRepository clubMemberRepository;

    @Override
    @Transactional
    public SuccessResponseDto createClubReview(Long clubId, ClubReviewRequestDto request) {
        List<ClubMember> clubMembers = clubMemberRepository.findByClubId(clubId);
        Optional<ClubMember> optionalClubMember = clubMembers.stream()
                .filter(cm -> cm.getUser().getStudentId().equals(request.studentId())).findAny();

        //후기 작성은 동아리 멤버만 가능
        if (optionalClubMember.isEmpty()) {
            log.warn("동아리에 가입되지 않은 학생입니다. clubId = {}, studentId = {}", clubId, request.studentId());
            throw new UnRegisteredUserException("리뷰를 작성하려면 동아리에 가입된 학생이어야 합니다.");
        }

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
