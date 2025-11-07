package com.kakaotech.team18.backend_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답 DTO")
public record ReissueResponseDto(
        @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidG9rZW5UeXBlIjoiQUNDRVNTIiwiaWF0IjoxNzE1MjMxMzU5LCJleHAiOjE3MTUyMzMxNTl9.example")
        String accessToken,
        @Schema(description = "새로 발급된 Refresh Token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidG9rZW5UeXBlIjoiUkVGUkVTSCIsImlhdCI6MTcxNTIzMTM1OSwiZXhwIjoxNzE1ODMwMTU5fQ.example")
        String refreshToken
) {
    public static ReissueResponseDto of(String accessToken, String refreshToken) {
        return new ReissueResponseDto(accessToken, refreshToken);
    }

    @Schema(description = "토큰 재발급 시 실제 클라이언트에게 전달되는 응답 본문")
    public record Body(
            @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidG9rZW5UeXBlIjoiQUNDRVNTIiwiaWF0IjoxNzE1MjMxMzU5LCJleHAiOjE3MTUyMzMxNTl9.example")
            String accessToken
    ) {}
}
