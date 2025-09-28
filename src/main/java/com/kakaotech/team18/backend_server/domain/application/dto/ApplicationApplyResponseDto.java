package com.kakaotech.team18.backend_server.domain.application.dto;

import java.time.LocalDateTime;

public record ApplicationApplyResponseDto(
        String studentId,
        LocalDateTime submittedAt,
        Boolean requiresConfirmation
) {
}