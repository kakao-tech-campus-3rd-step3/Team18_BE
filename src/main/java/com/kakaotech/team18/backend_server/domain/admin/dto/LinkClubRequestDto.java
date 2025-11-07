package com.kakaotech.team18.backend_server.domain.admin.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "동아리장(임원) - 동아리 연결 요청 DTO")
public record LinkClubRequestDto(

        @Schema(description = "동아리장 학번", example = "202312345")
        @NotBlank(message = "학번은 필수 값입니다.")
        String studentId,

        @Schema(description = "동아리 이름", example = "인터엑스")
        @NotBlank(message = "동아리 이름은 필수 값입니다.")
        String clubName,

        @Schema(description = "동아리 내 직책", example = "CLUB_ADMIN")
        @NotNull(message = "직책은 필수 값입니다.")
        Role role
) {
}