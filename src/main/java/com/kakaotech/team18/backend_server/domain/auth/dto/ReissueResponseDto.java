package com.kakaotech.team18.backend_server.domain.auth.dto;

public record ReissueResponseDto(
        String accessToken
) {
    public static ReissueResponseDto of(String accessToken) {
        return new ReissueResponseDto(accessToken);
    }
}
