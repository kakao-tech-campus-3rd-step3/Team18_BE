package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ClubDashboardApplicantResponseDto(
        @Schema(description = "지원자 목록")
        List<ApplicantResponseDto> applicants,

        @Schema(description = "메세지")
        String message
) {

}
