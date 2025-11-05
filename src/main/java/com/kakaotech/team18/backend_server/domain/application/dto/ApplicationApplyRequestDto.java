package com.kakaotech.team18.backend_server.domain.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.kakaotech.team18.backend_server.global.annotation.NoSpecialChar;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ApplicationApplyRequestDto(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "이름은 필수입니다.")
        @NoSpecialChar
        String name,

        @NotBlank(message = "학번은 필수입니다.")
        @Pattern(regexp = "\\d{6}", message = "학번은 6자리 숫자여야 합니다.")
        @NoSpecialChar
        String studentId,

        @Schema(description = "전화번호 (하이픈(-) 포함)", requiredMode = Schema.RequiredMode.REQUIRED, example = "010-1234-5678")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-xxxx-xxxx 형식이어야 합니다.")
        String phoneNumber,

        @NotBlank(message = "학과는 필수입니다.")
        String department,

        List<AnswerDto> answers
) {
        public record AnswerDto(
                Long questionNum,
                String question,
                JsonNode answer
        ) {}
}