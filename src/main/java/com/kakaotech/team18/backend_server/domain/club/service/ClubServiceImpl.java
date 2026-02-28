package com.kakaotech.team18.backend_server.domain.club.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.application.repository.InterviewSlotCountProjection;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashboardApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashboardApplicantResponseDto.InterviewDateSlotsDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashboardApplicantResponseDto.InterviewDateSlotsDto.InterviewSlotCountDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import com.kakaotech.team18.backend_server.domain.club.eventListener.ClubImageDeletedEvent;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubImageRepository;
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
import com.kakaotech.team18.backend_server.global.util.DateUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    private static final int INTERVIEW_SLOT_MINUTES = 30;

    private final ClubRepository clubRepository;
    private final ApplicationRepository applicationRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ClubImageRepository clubImageRepository;


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
        log.info("updateClubDetail called with clubId={}", clubId);
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
        clubApplyFormRepository
                .findByClubId(club.getId()).orElseThrow(() -> {
                    log.warn("ClubApplyForm not found for id={}", clubId);
                    return new ClubApplyFormNotFoundException("clubId = " + clubId);
                });
        List<ClubMember> applicantList = clubMemberRepository
                .findByClubIdAndRoleAndApplicationIsNotNull(clubId, Role.APPLICANT);
        List<ClubMember> pendingApplications = clubMemberRepository.findByClubIdAndRoleAndApplicationStatus(clubId, Role.APPLICANT, Status.PENDING);//role이 applicant 이면서 status가 pending인 지원서 수를 세어야함
        log.info("동아리 대쉬보드를 조회합니다 clubId={}, applicantList={}", clubId, applicantList);
        return new ClubDashBoardResponseDto(
                clubId,
                applicantList.size(),
                pendingApplications.size(),
                club.getRecruitStart().toLocalDate(),
                club.getRecruitEnd().toLocalDate(),
                club.getInterviewStartDate() != null ? club.getInterviewStartDate().toLocalDate() : null,
                club.getInterviewEndDate() != null ? club.getInterviewEndDate().toLocalDate() : null,
                (club.getInterviewStartTime() != null && club.getInterviewEndTime() != null) ?
                        DateUtil.formatTimeRange(club.getInterviewStartTime(), club.getInterviewEndTime()) : null);
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

        List<InterviewSlotCountProjection> rows = applicationRepository.countInterviewSlots(clubId);

        Map<LocalDate, Map<LocalTime, Integer>> grouped = buildInterviewScheduleTemplate(clubApplyForm.getClub());
        mergeAssignedInterviewCounts(grouped, rows != null ? rows : List.of());

        List<InterviewDateSlotsDto> interviewSchedule = convertToInterviewSchedule(grouped);
        return new ClubDashboardApplicantResponseDto(
                clubApplyForm.getClub().getIsInterviewRequired(),
                applicants
                        .stream()
                        .map(ApplicantResponseDto::from)
                        .toList(),
                interviewSchedule,
                message);
    }

    @Override
    @Transactional
    public SuccessResponseDto uploadClubImages(Long clubId, List<Long> keepImageId, List<MultipartFile> newImages) {
        Club findClub = clubRepository.findClubDetailById(clubId)
                .orElseThrow(() -> {
                    log.warn("Club not found for id={}", clubId);
                    return new ClubNotFoundException("clubId = " + clubId);
                });

        List<ClubImage> existingImages = clubImageRepository.findAllByClubId(clubId);

        // 삭제 대상 URL 추출
        List<String> deleteTargetUrls = existingImages.stream()
                .filter(img -> keepImageId == null || !keepImageId.contains(img.getId()))
                .map(ClubImage::getImageUrl)
                .toList();

        if (keepImageId == null || keepImageId.isEmpty()) {
            findClub.getIntroduction().getImages().clear();
        } else {
            findClub.getIntroduction().getImages().removeIf(img -> !keepImageId.contains(img.getId()));
        }
        log.info("Successfully deleted old images for clubId: {}", clubId);

        // 새 이미지 업로드
        List<String> newImageUrls = new ArrayList<>();
        try {
            if (newImages != null) {
                for (MultipartFile image : newImages) {
                    newImageUrls.add(s3Service.upload(image));
                }
            }
        } catch (Exception e) {
            log.warn("S3 업로드 중 오류 → 보상 트랜잭션 실행. clubId={}, error={}", clubId, e.getMessage());
            newImageUrls.forEach(url -> {
                try { s3Service.deleteFile(url); } catch (Exception ignore) {}
            });
            throw e;
        }
        findClub.getIntroduction().addImages(newImageUrls);
        log.info("Successfully uploaded and updated images for clubId: {}", clubId);
        applicationEventPublisher.publishEvent(new ClubImageDeletedEvent(clubId, deleteTargetUrls));
        return new SuccessResponseDto(true);
    }

    private Map<LocalDate, Map<LocalTime, Integer>> buildInterviewScheduleTemplate(Club club) {
        Map<LocalDate, Map<LocalTime, Integer>> schedule = new TreeMap<>();
        if (!Boolean.TRUE.equals(club.getIsInterviewRequired())) {
            return schedule;
        }

        LocalDateTime interviewStartDateTime = club.getInterviewStartDate();
        LocalDateTime interviewEndDateTime = club.getInterviewEndDate();
        LocalTime interviewStartTime = club.getInterviewStartTime();
        LocalTime interviewEndTime = club.getInterviewEndTime();

        if (interviewStartDateTime == null || interviewEndDateTime == null
                || interviewStartTime == null || interviewEndTime == null) {
            return schedule;
        }

        LocalDate startDate = interviewStartDateTime.toLocalDate();
        LocalDate endDate = interviewEndDateTime.toLocalDate();

        if (startDate.isAfter(endDate)) {
            return schedule;
        }
        if (!interviewStartTime.isBefore(interviewEndTime)) {
            return schedule;
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<LocalTime, Integer> slots = new TreeMap<>();
            for (LocalTime time = interviewStartTime; time.isBefore(interviewEndTime); time = time.plusMinutes(INTERVIEW_SLOT_MINUTES)) {
                slots.put(time, 0);
            }
            schedule.put(date, slots);
        }
        return schedule;
    }

    private void mergeAssignedInterviewCounts(Map<LocalDate, Map<LocalTime, Integer>> grouped, List<InterviewSlotCountProjection> rows) {
        for (InterviewSlotCountProjection row : rows) {
            LocalDate date = row.getInterviewDate();
            LocalTime time = row.getInterviewTime();
            if (date == null || time == null) {
                continue;
            }
            Map<LocalTime, Integer> slots = grouped.get(date);
            if (slots == null || !slots.containsKey(time)) {
                continue;
            }
            slots.put(time, (int) row.getAssignedCount());
        }
    }

    private List<InterviewDateSlotsDto> convertToInterviewSchedule(Map<LocalDate, Map<LocalTime, Integer>> grouped) {
        return grouped.entrySet().stream()
                .map(dateEntry -> new InterviewDateSlotsDto(
                        dateEntry.getKey(),
                        dateEntry.getValue().entrySet().stream()
                                .map(timeEntry ->
                                        new InterviewSlotCountDto(
                                                timeEntry.getKey(),
                                                timeEntry.getValue()
                                        )
                                )
                                .toList()
                ))
                .toList();
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
