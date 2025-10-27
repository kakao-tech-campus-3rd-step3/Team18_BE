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

        @Schema(description = "전화번호 (하이픈(-) 제외)", example = "01012345678")
        @Pattern(regexp = "^\\d{10,11}$", message = "올바른 전화번호 형식이 아닙니다.")
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