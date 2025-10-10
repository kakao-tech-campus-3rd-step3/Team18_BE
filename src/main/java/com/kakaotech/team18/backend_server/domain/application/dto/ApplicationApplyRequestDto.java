package com.kakaotech.team18.backend_server.domain.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ApplicationApplyRequestDto(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "이름은 필수입니다.")
        String name,


        @NotBlank(message = "학번은 필수입니다.")
        String studentId,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phoneNumber,

        @NotBlank(message = "학과는 필수입니다.")
        String department,

        List<AnswerDto> answers
) {
        public record AnswerDto(
                Long questionNum,
                String question,
                String answer
        ) {}
}