package com.kakaotech.team18.backend_server.domain.application.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateRequestDto(
    @NotNull(message = "상태 값은 비어 있을 수 없습니다.")
    Status status
) {
}
