package com.kakaotech.team18.backend_server.domain.club.dto;


import com.kakaotech.team18.backend_server.domain.applicant.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import java.util.List;

/**
 * 총 지원자 수
 * 대기중인 지원서 수
 * 모집 일정 정보
 * 지원자 목록 조회 (이름, 학번, 학과, 전화번호, 이메일, 결과)
 */

public record ClubDashBoardResponseDto(
        int totalApplicantCount,
        int pendingApplicationCount,
        String startDay,
        String endDay,
        List<ApplicantResponseDto> applicants
) {
}
