package com.kakaotech.team18.backend_server.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoUserInfoResponseDto {

    private Long id; // 사용자의 고유 ID (kakaoId)

    @JsonProperty("properties")
    private Properties properties;

    @Data
    @NoArgsConstructor
    public static class Properties {
        private String nickname;
    }
}
