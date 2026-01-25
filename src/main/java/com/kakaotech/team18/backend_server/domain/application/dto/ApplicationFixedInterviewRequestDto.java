package com.kakaotech.team18.backend_server.domain.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "지원자 인터뷰 일정 확정 요청 데이터")
public record ApplicationFixedInterviewRequestDto(
        @Schema(description = "지원자 인터뷰 일시", example = "2026-02-10T10:00:00")
        @NotNull(message = "인터뷰 일시는 필수입니다.")
        LocalDateTime interviewAt
)
{}
