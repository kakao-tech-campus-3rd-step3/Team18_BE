package com.kakaotech.team18.backend_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Schema(description = "최종 회원가입을 위한 추가 정보 요청 데이터")
public record RegisterRequestDto(
    @Schema(description = "이름(실명)", requiredMode = Schema.RequiredMode.REQUIRED, example = "김지원")
    @NotEmpty(message = "이름은 비어 있을 수 없습니다.")
    String name,

    @Schema(description = "이메일", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotEmpty(message = "이메일은 비어 있을 수 없습니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @Schema(description = "학번", requiredMode = Schema.RequiredMode.REQUIRED, example = "213456")
    @NotEmpty(message = "학번은 비어 있을 수 없습니다.")
    String studentId,

    @Schema(description = "학과", requiredMode = Schema.RequiredMode.REQUIRED, example = "컴퓨터공학과")
    @NotEmpty(message = "학과는 비어 있을 수 없습니다.")
    String department,

    @Schema(description = "전화번호 (하이픈(-) 제외)", requiredMode = Schema.RequiredMode.REQUIRED, example = "01012345678")
    @Pattern(regexp = "^\\d{10,11}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phoneNumber
) {
}
