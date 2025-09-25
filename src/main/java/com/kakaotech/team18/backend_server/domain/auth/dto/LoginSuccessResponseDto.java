package com.kakaotech.team18.backend_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 성공 시 응답 데이터")
public record LoginSuccessResponseDto(
    @Schema(description = "응답 상태", example = "LOGIN_SUCCESS")
    String status,

    @Schema(description = "우리 서비스의 Access Token")
    String accessToken,

    @Schema(description = "Access Token 재발급을 위한 Refresh Token")
    String refreshToken
) implements LoginResponse {
    // LoginResponse 인터페이스를 구현합니다.
}
