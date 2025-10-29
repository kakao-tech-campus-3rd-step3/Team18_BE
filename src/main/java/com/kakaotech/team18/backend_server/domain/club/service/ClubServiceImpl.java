package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashboardApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import com.kakaotech.team18.backend_server.domain.club.eventListener.ClubImageDeletedEvent;
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
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubMemberNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import com.kakaotech.team18.backend_server.global.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ApplicationRepository applicationRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher applicationEventPublisher;


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
                    return new ClubMemberNotFoundException("해당 동아리의 동아리 회장을 찾을 수 없습니다 clubId = " + findClub.getId());
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
                clubId,
                applicantList.size(),
                pendingApplication.size(),
                club.getRecruitStart().toLocalDate(),
                club.getRecruitEnd().toLocalDate());
    }

    @Override
    public ClubDashboardApplicantResponseDto getApplicantsByStatusAndStage(Long clubId, Status status, Stage stage) {
        List<ClubMember> applicants;
        String message;
        if (status != null) {
            applicants = clubMemberRepository.findByClubIdAndRoleAndApplicationStatusAndStage(clubId, Role.APPLICANT, status, stage);
        } else {
            applicants = clubMemberRepository.findByClubIdAndRoleAndStage(clubId, Role.APPLICANT, stage);
        }
        ClubApplyForm clubApplyForm = clubApplyFormRepository
                .findByClubId(clubId).orElseThrow(() -> {
                    log.warn("ClubApplyForm not found for id={}", clubId);
                    return new ClubApplyFormNotFoundException("clubId = " + clubId);
                });
        if (stage == Stage.INTERVIEW){
            message = clubApplyForm.getInterviewMessage();
        } else {
            message = clubApplyForm.getFinalMessage();
        }
        return new ClubDashboardApplicantResponseDto(
                applicants
                        .stream()
                        .map(ApplicantResponseDto::from)
                        .toList(),
                message);
    }

    @Override
    @Transactional
    public SuccessResponseDto uploadClubImages(Long clubId, List<MultipartFile> images) {
        Club findClub = clubRepository.findClubDetailById(clubId)
                .orElseThrow(() -> {
                    log.warn("Club not found for id={}", clubId);
                    return new ClubNotFoundException("clubId = " + clubId);
                });

        // 기존 이미지 URL 백업
        List<String> oldImageUrls = findClub.getIntroduction().getImages()
                .stream()
                .map(ClubImage::getImageUrl)
                .toList();

        List<String> imageUrls = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                imageUrls.add(s3Service.upload(image));
            }
        } catch (Exception e) {
            // 이미 업로드된 새로운 이미지들 삭제 (보상 트랜잭션)
            log.warn("S3 이미지 업로드 중 오류 발생. 롤백을 위해 업로드된 이미지 삭제 시도 for clubId: {}, [error : {}]", clubId, e.getMessage());
            imageUrls.forEach(url -> {
                try { s3Service.deleteFile(url); } catch (Exception ignore) {}
            });
            throw e; // 그대로 예외 던져서 트랜잭션 롤백
        }
        log.info("S3에 이미지 업로드 완료 for clubId: {}", clubId);

        findClub.getIntroduction().updateImages(imageUrls);
        log.info("Successfully uploaded and updated images for clubId: {}", clubId);

        // 커밋 이후 삭제를 위해 이벤트 발행
        applicationEventPublisher.publishEvent(new ClubImageDeletedEvent(clubId, oldImageUrls));
        return new SuccessResponseDto(true);
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
