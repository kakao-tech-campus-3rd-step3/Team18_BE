package com.kakaotech.team18.backend_server.domain.application.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "지원서 상태 변경 요청 데이터")
public record ApplicationStatusUpdateRequestDto(
    @Schema(description = "변경할 상태 (PENDING, APPROVED, REJECTED 중 하나)", requiredMode = Schema.RequiredMode.REQUIRED, example = "APPROVED")
    @NotNull(message = "상태 값은 비어 있을 수 없습니다.")
    Status status
) {
}
