package com.kakaotech.team18.backend_server.domain.club.dto;


import com.kakaotech.team18.backend_server.domain.applicant.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import java.util.List;

public record ClubDashBoardResponseDto(
        int totalApplicantCount,
        int pendingApplicationCount,
        String startDay,
        String endDay,
        List<ApplicantResponseDto> applicants
) {
}
