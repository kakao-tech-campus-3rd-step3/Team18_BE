package com.kakaotech.team18.backend_server.domain.club.dto;


import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "동아리 대시보드 조회 응답 데이터")
public record ClubDashBoardResponseDto(
        @Schema(description = "총 지원자 수", example = "50")
        int totalApplicantCount,
        @Schema(description = "현재 대기중인 지원서 수", example = "15")
        int pendingApplicationCount,
        @Schema(description = "모집 시작일 (yyyy-MM-dd 형식)", example = "2024-09-01")
        LocalDate startDay,
        @Schema(description = "모집 마감일 (yyyy-MM-dd 형식)", example = "2024-09-15")
        LocalDate endDay
) {
}
