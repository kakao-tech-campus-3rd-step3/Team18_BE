package com.kakaotech.team18.backend_server.domain.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApplicationApplyResponseDto(
        String studentId,
        LocalDateTime submittedAt,
        Boolean requiresConfirmation
) {
}