package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubMemberNotFoudException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.util.List;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ApplicationRepository applicationRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;


    @Override
    public ClubListResponseDto getClubByCategory(String category) {
        if (category.equals("ALL")) {
            return mapToResponse(clubRepository.findAllProjectedBy());
        }
        return mapToResponse(clubRepository.findSummariesByCategory(Category.valueOf(category)));
    }

    @Override
    public ClubListResponseDto getClubByName(String name) {
        if (name == null || name.isBlank()) {
            return mapToResponse(clubRepository.findAllProjectedBy());
        }
        return mapToResponse(clubRepository.findSummariesByNameContaining(name));
    }

    @Override
    public ClubListResponseDto getAllClubs() {
        return mapToResponse(clubRepository.findAllProjectedBy());
    }

    @Override
    public ClubDetailResponseDto getClubDetail(Long clubId) {
        log.info("getClubDetail called with clubId={}", clubId);
        Club findClub = clubRepository.findClubDetailById(clubId)
                .orElseThrow(() -> {
                    log.warn("Club not found for id={}", clubId);
                    return new ClubNotFoundException("clubId = " + clubId);
                });
        log.info("Successfully found clubDetail: {}", findClub.getName());
        ClubMember clubAdmin = clubMemberRepository.findClubAdminByClubIdAndRole(findClub.getId(), Role.CLUB_ADMIN)
                .orElseThrow(() -> {
                    log.warn("ClubAdmin not found for id={}", findClub.getId());
                    return new ClubMemberNotFoudException("해당 동아리의 동아리 회장을 찾을 수 없습니다 clubId = " + findClub.getId());
                });
        return ClubDetailResponseDto.from(findClub, clubAdmin.getUser());
    }

    @Override
    @Transactional
    public SuccessResponseDto updateClubDetail(Long clubId, ClubDetailRequestDto dto){
        log.info("getClubDetail called with clubId={}", clubId);
        Club findClub = clubRepository.findClubDetailById(clubId)
                .orElseThrow(() -> {
                    log.warn("Club not found for id={}", clubId);
                    return new ClubNotFoundException("clubId = " + clubId);
                });
        log.info("Successfully found clubDetail: {}", findClub.getName());
        findClub.updateDetail(dto);
        return new SuccessResponseDto(true);
    }

    @Override
    public ClubDashBoardResponseDto getClubDashBoard(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> {
                    log.warn("Club not found for id={}", clubId);
                    return new ClubNotFoundException("clubId = " + clubId);
                });
        ClubApplyForm clubApplyForm = clubApplyFormRepository
                .findByClubId(club.getId()).orElseThrow(() -> {
                    log.warn("ClubApplyForm not found for id={}", clubId);
                    return new ClubApplyFormNotFoundException("clubId = " + clubId);
                });
        List<ClubMember> applicantList = clubMemberRepository.findByClubIdAndRole(clubId, Role.APPLICANT);
        List<Application> pendingApplication = applicationRepository.findByClubApplyFormIdAndStatus(clubApplyForm.getId(), Status.PENDING);
        log.info("동아리 대쉬보드를 조회합니다 clubId={}, applicantList={}", clubId, applicantList);
        return new ClubDashBoardResponseDto(
                applicantList.size(),
                pendingApplication.size(),
                club.getRecruitStart().toLocalDate(),
                club.getRecruitEnd().toLocalDate(),
                applicantList.stream()
                        .map(ApplicantResponseDto::from)
                        .toList());
    }

    @Override
    public List<ApplicantResponseDto> getApplicantsByStatus(Long clubId, Status status) {
        if (status != null) {
            List<ClubMember> filteredApplicants = clubMemberRepository.findByClubIdAndRoleAndApplicationStatus(clubId, Role.APPLICANT, status);
            return filteredApplicants
                    .stream()
                    .map(ApplicantResponseDto::from)
                    .toList();
        } else {
            List<ClubMember> allApplicants = clubMemberRepository.findByClubIdAndRole(clubId, Role.APPLICANT);
            return allApplicants
                    .stream()
                    .map(ApplicantResponseDto::from)
                    .toList();
        }
    }

    // ---- private helpers ----
    private ClubListResponseDto mapToResponse(List<ClubSummary> summaries) {
        List<ClubListResponseDto.ClubsInfo> clubs = summaries.stream()
                .map(summary -> ClubListResponseDto.from(
                        summary,
                        RecruitStatusCalculator.calculate(summary.getRecruitStart(), summary.getRecruitEnd()).getDisplayName()
                ))
                .toList();

        return new ClubListResponseDto(clubs);
    }
}
