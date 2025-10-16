package com.kakaotech.team18.backend_server.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    long accessTokenValidityInSeconds,
    long refreshTokenValidityInSeconds
) {
}
