package com.kakaotech.team18.backend_server.domain.auth.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubIdAndRoleInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "로그인 성공 시 응답 데이터")
public record LoginSuccessResponseDto(
        @Schema(description = "응답 상태", example = "LOGIN_SUCCESS")
        AuthStatus status,

        @Schema(description = "우리 서비스의 Access Token")
        String accessToken,

        @Schema(description = "Access Token 재발급을 위한 Refresh Token")
        String refreshToken,

        @Schema(description = "clubId와 Role 정보를 담은 리스트")
        List<ClubIdAndRoleInfoDto> clubIdAndRoleList
) implements LoginResponse {
    @Schema(description = "로그인 성공 시 실제 클라이언트에게 전달되는 응답 본문")
    public record Body(
        @Schema(description = "응답 상태", example = "LOGIN_SUCCESS")
        AuthStatus status,

        @Schema(description = "우리 서비스의 Access Token")
        String accessToken
    ) implements LoginResponse {
        // LoginResponse 인터페이스를 구현합니다.
    }
}
