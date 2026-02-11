package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "동아리원 정보 수정 요청 DTO (변경할 필드만 포함)")
public record ClubMemberUpdateRequestDto(
        @Schema(description = "이름", example = "박개명")
        String name,

        @Schema(description = "학번", example = "212121")
        String studentId,

        @Schema(description = "전화번호", example = "010-9999-8888")
        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (010-XXXX-XXXX)")
        String phoneNumber,

        @Schema(description = "단과대학", example = "AI융합대학")
        String college,

        @Schema(description = "학과", example = "인공지능학부")
        String department,

        @Schema(description = "학적상태", example = "LEAVE_OF_ABSENCE")
        AcademicStatus academicStatus,

        @Schema(description = "가입일자 (YYYY-MM)", example = "2024-05")
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "가입일자 형식이 올바르지 않습니다. (YYYY-MM)")
        String joinDate
) {
}
