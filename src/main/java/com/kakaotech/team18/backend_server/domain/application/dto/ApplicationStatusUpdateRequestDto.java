package com.kakaotech.team18.backend_server.domain.application.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;

public record ApplicationStatusUpdateRequestDto(
    Status status
) {
}
