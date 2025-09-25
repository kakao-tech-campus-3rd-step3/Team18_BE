package com.kakaotech.team18.backend_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "카카오 로그인 요청 데이터")
public record KakaoLoginRequestDto(
    @Schema(description = "카카오 서버로부터 받은 인가 코드", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "인가 코드는 비어 있을 수 없습니다.")
    String authorizationCode
) {
}
