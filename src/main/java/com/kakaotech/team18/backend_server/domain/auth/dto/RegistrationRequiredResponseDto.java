package com.kakaotech.team18.backend_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추가 정보 입력 필요 시 응답 데이터")
public record RegistrationRequiredResponseDto(
    @Schema(description = "응답 상태", example = "REGISTRATION_REQUIRED")
    String status,

    @Schema(description = "회원가입 완료를 위해 필요한 임시 토큰")
    String temporaryToken
) implements LoginResponse {
    // LoginResponse 인터페이스를 구현합니다.
}
