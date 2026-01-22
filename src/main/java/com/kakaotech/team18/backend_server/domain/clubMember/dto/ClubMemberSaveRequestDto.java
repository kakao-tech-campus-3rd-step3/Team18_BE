package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "동아리원 등록/수정 요청 DTO")
public record ClubMemberSaveRequestDto(
        @Schema(description = "이름", example = "박신입")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "학번", example = "241001")
        @NotBlank(message = "학번은 필수입니다.")
        String studentId,

        @Schema(description = "전화번호", example = "010-1111-2222")
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (010-XXXX-XXXX)")
        String phoneNumber,

        @Schema(description = "단과대학", example = "공과대학")
        @NotBlank(message = "단과대학은 필수입니다.")
        String college,

        @Schema(description = "학과", example = "컴퓨터공학과")
        @NotBlank(message = "학과는 필수입니다.")
        String department,

        @Schema(description = "학적상태", example = "ENROLLED")
        @NotNull(message = "학적상태는 필수입니다.")
        AcademicStatus academicStatus,

        @Schema(description = "직책", example = "CLUB_MEMBER")
        @NotNull(message = "직책은 필수입니다.")
        Role role,

        @Schema(description = "가입일자 (YYYY-MM)", example = "2024-03")
        @NotBlank(message = "가입일자는 필수입니다.")
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "가입일자 형식이 올바르지 않습니다. (YYYY-MM)")
        String joinDate
) {
}
