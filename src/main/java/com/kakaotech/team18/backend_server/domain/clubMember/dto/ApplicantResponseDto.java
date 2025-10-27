package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 내 지원자 목록의 개별 지원자 정보")
public record ApplicantResponseDto(
        @Schema(description = "지원자 이름", example = "김지원")
        String name,
        @Schema(description = "학번", example = "212121")
        String studentId,
        @Schema(description = "학과", example = "컴퓨터공학과")
        String department,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,
        @Schema(description = "이메일", example = "test@test.com")
        String email,
        @Schema(description = "지원서 상태", example = "PENDING")
        Status status,
        @Schema(description = "지원서 ID", example = "1")
        Long applicantId
) {

    public static ApplicantResponseDto from(ClubMember clubMember) {
        return new ApplicantResponseDto(
                clubMember.getUser().getName(),
                clubMember.getUser().getStudentId(),
                clubMember.getUser().getDepartment(),
                clubMember.getUser().getPhoneNumber(),
                clubMember.getUser().getEmail(),
                clubMember.getApplication().getStatus(),
                clubMember.getApplication().getId()
        );
    }
}
