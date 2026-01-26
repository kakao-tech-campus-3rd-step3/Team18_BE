package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ClubDashboardApplicantResponseDto(
        @Schema(description = "지원자 목록")
        List<ApplicantResponseDto> applicants,

        List<InterviewDateSlotsDto> interviewSchedule,

        @Schema(description = "메세지")
        String message
) {
    public record InterviewDateSlotsDto(
            LocalDate date,
            List<InterviewSlotCountDto> slots
    ) {
        public record InterviewSlotCountDto(
                LocalTime time,
                int assignedCount
        ) {}
    }
}
