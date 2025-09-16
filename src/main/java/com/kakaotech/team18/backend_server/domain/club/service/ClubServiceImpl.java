package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.applicant.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.applicant.entity.Applicant;
import com.kakaotech.team18.backend_server.domain.applicant.repository.ApplicantRepository;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicationRepository applicationRepository;


    @Override
    public List<ClubListResponseDto> getClubByCategory(Category category) {
        if (category == null) {
            return mapToResponse(clubRepository.findAllSummaries());
        }
        return mapToResponse(clubRepository.findSummariesByCategory(category));
    }

    @Override
    public List<ClubListResponseDto> getClubByName(String name) {
        if (name == null || name.isBlank()) {
            return mapToResponse(clubRepository.findAllSummaries());
        }
        return mapToResponse(clubRepository.findSummariesByNameContaining(name));
    }

    @Override
    public List<ClubListResponseDto> getAllClubs() {
        return mapToResponse(clubRepository.findAllSummaries());
    }

    @Override
    public ClubDetailResponseDto getClubDetail(Long clubId) {
        log.info("getClubDetail called with clubId={}", clubId);
        Club findClub = clubRepository.findById(clubId)
                .orElseThrow(() -> {
                    log.warn("Club not found for id={}", clubId);
                    return new ClubNotFoundException("clubId = " + clubId);
                });
        log.info("Successfully found clubDetail: {}", findClub.getName());
        return ClubDetailResponseDto.from(findClub, findClub.getPresident());
    }

    @Override
    public ClubDashBoardResponseDto getClubDashBoard(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> {
                    log.warn("Club not found for id={}", clubId);
                    return new ClubNotFoundException("clubId = " + clubId);
                });
        List<Applicant> applicantList = applicantRepository.findByClubId(clubId);
        List<Application> pendingApplication = applicationRepository.findByClubIdAndStatus(clubId, Status.PENDING);
        log.info("동아리 대쉬보드를 조회합니다 clubId={}, applicantList={}", clubId, applicantList);
        return new ClubDashBoardResponseDto(
                applicantList.size(),
                pendingApplication.size(),
                club.getRecruitStart().toLocalDate().toString(),
                club.getRecruitEnd().toLocalDate().toString(),
                applicantList.stream()
                        .map(ApplicantResponseDto::from)
                        .toList());
    }

    // ---- private helpers ----
    private List<ClubListResponseDto> mapToResponse(List<ClubSummary> summaries) {
        return summaries.stream()
                .map(s -> ClubListResponseDto.from(
                        s,
                        calculateRecruitStatus(s.getRecruitStart(), s.getRecruitEnd())
                ))
                .toList();
    }

    private String calculateRecruitStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime today = LocalDateTime.now();

        if (start == null || end == null) {
            return "모집 일정 미정";
        }
        if (today.isBefore(start)) {
            return "모집 준비중";
        }
        if (!today.isBefore(end)) {
            return "모집 종료";
        }
        return "모집중";
    }
}
