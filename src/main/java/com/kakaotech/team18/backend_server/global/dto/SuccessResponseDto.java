package com.kakaotech.team18.backend_server.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단순 성공 여부 응답 데이터")
public record SuccessResponseDto(
    @Schema(description = "작업 성공 여부", example = "true")
    Boolean success
) {
}
