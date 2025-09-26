package com.kakaotech.team18.backend_server.domain.club.dto;


import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import java.util.List;

public record ClubDashBoardResponseDto(
        int totalApplicantCount,
        int pendingApplicationCount,
        String startDay,
        String endDay,
        List<ApplicantResponseDto> applicants
) {
}
