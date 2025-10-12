package com.kakaotech.team18.backend_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답 DTO")
public record ReissueResponseDto(
        @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidG9rZW5UeXBlIjoiQUNDRVNTIiwiaWF0IjoxNzE1MjMxMzU5LCJleHAiOjE3MTUyMzMxNTl9.example")
        String accessToken
) {
    public static ReissueResponseDto of(String accessToken) {
        return new ReissueResponseDto(accessToken);
    }
}
